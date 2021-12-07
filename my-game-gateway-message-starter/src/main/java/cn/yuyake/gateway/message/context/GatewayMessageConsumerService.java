package cn.yuyake.gateway.message.context;

import cn.yuyake.common.concurrent.GameEventExecutorGroup;
import cn.yuyake.dao.PlayerDao;
import cn.yuyake.game.GameMessageService;
import cn.yuyake.game.bus.GameMessageInnerDecoder;
import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.gateway.message.channel.GameChannelConfig;
import cn.yuyake.gateway.message.channel.GameChannelInitializer;
import cn.yuyake.gateway.message.channel.GameMessageEventDispatchService;
import cn.yuyake.gateway.message.channel.IMessageSendFactory;
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

    // 启动客户端消息处理，这里需要手动传进来处理消息的Handler
    public void start(GameChannelInitializer gameChannelInitializer) {
        workerGroup = new GameEventExecutorGroup(serverConfig.getWorkerThreads());
        gameGatewayMessageSendFactory = new GameGatewayMessageSendFactory(kafkaTemplate, serverConfig.getGatewayGameMessageTopic());
        gameChannelService = new GameMessageEventDispatchService(context, workerGroup, gameGatewayMessageSendFactory, gameChannelInitializer);
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
}
