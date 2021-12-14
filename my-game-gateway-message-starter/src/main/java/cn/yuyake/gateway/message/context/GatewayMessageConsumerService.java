package cn.yuyake.gateway.message.context;

import cn.yuyake.common.cloud.PlayerServiceInstance;
import cn.yuyake.common.concurrent.GameEventExecutorGroup;
import cn.yuyake.dao.PlayerDao;
import cn.yuyake.game.GameMessageService;
import cn.yuyake.game.bus.GameMessageInnerDecoder;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.gateway.message.channel.GameChannelConfig;
import cn.yuyake.gateway.message.channel.GameChannelInitializer;
import cn.yuyake.gateway.message.channel.GameMessageEventDispatchService;
import cn.yuyake.gateway.message.channel.IMessageSendFactory;
import cn.yuyake.gateway.message.rpc.GameRpcService;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 用于接收网关消息，并分发消息到业务中
 */
@Service
public class GatewayMessageConsumerService {
    private final Logger logger = LoggerFactory.getLogger(GatewayMessageConsumerService.class);
    // 默认实现的消息发送接口，GameChannel返回的消息通过此接口发送到kafka中
    private IMessageSendFactory gameGatewayMessageSendFactory;
    // 消息事件分类发，负责将用户的消息发到相应的GameChannel之中
    private GameMessageEventDispatchService gameChannelService;
    // 业务处理的线程池
    private GameEventExecutorGroup workerGroup;
    // RPC服务
    private GameRpcService gameRpcSendFactory;
    // RPC处理的线程池
    private EventExecutorGroup rpcWorkerGroup = new DefaultEventExecutorGroup(2);
    @Autowired // GameChannel的一些配置信息
    private GameChannelConfig serverConfig;
    @Autowired // 消息管理类，负责管理根据消息id，获取对应的消息类实例
    private GameMessageService gameMessageService;
    @Autowired // kafka客户端类
    private KafkaTemplate<String, byte[]> kafkaTemplate;
    @Autowired // 用户数据库操作类
    private PlayerDao playerDao;
    @Autowired // 上下文
    private ApplicationContext context;
    @Autowired
    private PlayerServiceInstance playerServiceInstance;

    // 启动客户端消息处理，这里需要手动传进来处理消息的Handler
    public void start(GameChannelInitializer gameChannelInitializer, int localServerId) {
        workerGroup = new GameEventExecutorGroup(serverConfig.getWorkerThreads());
        gameGatewayMessageSendFactory = new GameGatewayMessageSendFactory(kafkaTemplate, serverConfig.getGatewayGameMessageTopic());
        gameRpcSendFactory = new GameRpcService(serverConfig.getRpcRequestGameMessageTopic(), serverConfig.getRpcResponseGameMessageTopic(), localServerId, playerServiceInstance, rpcWorkerGroup, kafkaTemplate);
        gameChannelService = new GameMessageEventDispatchService(context, workerGroup, gameGatewayMessageSendFactory, gameRpcSendFactory, gameChannelInitializer);
    }

    // 指定监听的topic和组ID
    @KafkaListener(topics = {"${game.channel.business-game-message-topic}" + "-" + "${game.server.config.server-id}"}, groupId = "${game.channel.topic-group-id}")
    public void consume(ConsumerRecord<String, byte[]> record) {
        // 读取消息内容
        GameMessagePackage gameMessagePackage = GameMessageInnerDecoder.readGameMessagePackage(record.value());
        GameMessageHeader header = gameMessagePackage.getHeader();
        logger.debug("接收网关消息：{}", header);
        // 转化为消息类
        IGameMessage gameMessage = gameMessageService.getRequestInstanceByMessageId(header.getMessageId());
        gameMessage.read(gameMessagePackage.getBody());
        gameMessage.setHeader(header);
        // 发送到GameChannel中
        gameChannelService.fireReadMessage(header.getPlayerId(), gameMessage);
    }

    @KafkaListener(topics = {"${game.channel.rpc-request-game-message-topic}" + "-" + "${game.server.config.server-id}"}, groupId = "rpc-${game.channel.topic-group-id}")
    public void consumeRPCRequestMessage(ConsumerRecord<String, byte[]> record) {
        // 获取从消息总线服务中监听到的RPC请求消息
        IGameMessage gameMessage = this.getGameMessage(EnumMessageType.RPC_REQUEST, record.value());
        // 将收到的RPC请求消息发送到GameChannel中处理
        gameChannelService.fireReadRPCRequest(gameMessage);
    }

    @KafkaListener(topics = {"${game.channel.rpc-response-game-message-topic}" + "-" + "${game.server.config.server-id}"}, groupId = "rpc-request-${game.channel.topic-group-id}")
    public void consumeRPCResponseMessage(ConsumerRecord<String, byte[]> record) {
        IGameMessage gameMessage = this.getGameMessage(EnumMessageType.RPC_RESPONSE, record.value());
        this.gameRpcSendFactory.receiveResponse(gameMessage);
    }

    // 从接收到的数据流中反序列化消息
    private IGameMessage getGameMessage(EnumMessageType messageType, byte[] data) {
        GameMessagePackage gameMessagePackage = GameMessageInnerDecoder.readGameMessagePackage(data);
        logger.debug("收到{}消息：{}", messageType, gameMessagePackage.getHeader());
        GameMessageHeader header = gameMessagePackage.getHeader();
        IGameMessage gameMessage = gameMessageService.getMessageInstance(messageType, header.getMessageId());
        gameMessage.read(gameMessagePackage.getBody());
        gameMessage.setHeader(header);
        gameMessage.getHeader().setMessageType(messageType);
        return gameMessage;
    }
}
