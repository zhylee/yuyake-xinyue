package cn.yuyake.gateway.message.rpc;

import cn.yuyake.common.cloud.PlayerServiceInstance;
import cn.yuyake.common.utils.TopicUtil;
import cn.yuyake.game.bus.GameMessageInnerDecoder;
import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.game.common.IGameMessage;
import io.netty.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.atomic.AtomicInteger;

public class GameRpcService {
    private static final Logger logger = LoggerFactory.getLogger(GameRpcService.class);
    // 自增的唯一序列Id
    private final AtomicInteger seqId = new AtomicInteger();
    // 本地服务实例ID
    private final int localServerId;
    // request topic
    private final String requestTopic;
    // response topic
    private final String responseTopic;
    // 负载均衡管理
    private final PlayerServiceInstance playerServiceInstance;
    // RPC处理的线程池
    private final EventExecutorGroup eventExecutorGroup;
    // kafka template
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    // RPC回调服务
    private final GameRpcCallbackService gameRpcCallbackService;

    public GameRpcService(
            String requestTopic,
            String responseTopic,
            int localServerId,
            PlayerServiceInstance playerServiceInstance,
            EventExecutorGroup eventExecutorGroup,
            KafkaTemplate<String, byte[]> kafkaTemplate
    ) {
        this.requestTopic = requestTopic;
        this.responseTopic = responseTopic;
        this.localServerId = localServerId;
        this.playerServiceInstance = playerServiceInstance;
        this.eventExecutorGroup = eventExecutorGroup;
        this.kafkaTemplate = kafkaTemplate;
        this.gameRpcCallbackService = new GameRpcCallbackService(eventExecutorGroup);
    }

    public void sendRPCRequest(IGameMessage gameMessage, Promise<IGameMessage> promise) {
        GameMessagePackage gameMessagePackage = new GameMessagePackage();
        gameMessagePackage.setHeader(gameMessage.getHeader());
        gameMessagePackage.setBody(gameMessage.body());
        GameMessageHeader header = gameMessage.getHeader();
        // 自增一个唯一的序列ID，作为此次发送消息的标识符，当消息返回时，需要携带回来
        header.setClientSeqId(seqId.incrementAndGet());
        // 设置发送消息的服务器ID，用于告诉目标服务返回消息时使用
        header.setFromServerId(localServerId);
        // 发送的时间，用于测试消息的传输时间
        header.setClientSendTime(System.currentTimeMillis());
        // 发送RPC消息的角色ID
        long playerId = header.getPlayerId();
        // 目标服务ID
        int serviceId = header.getServiceId();
        // 根据目标的服务ID，从目标服务中选择一个处理消息的服务实例ID，即serverId
        Promise<Integer> selectServerIdPromise = new DefaultPromise<>(this.eventExecutorGroup.next());
        playerServiceInstance.selectServerId(playerId, serviceId, selectServerIdPromise).addListener((GenericFutureListener<Future<Integer>>) future -> {
            if (future.isSuccess()) {
                header.setToServerId(future.get());
                // 动态创建游戏网关监听消息的topic
                String sendTopic = TopicUtil.generateTopic(requestTopic, gameMessage.getHeader().getToServerId());
                GameMessageInnerDecoder.sendMessage(kafkaTemplate, gameMessagePackage, sendTopic);
                // 记录回调方法，当请求消息返回时，调用回调方法
                gameRpcCallbackService.addCallback(header.getClientSeqId(), promise);
            } else {
                logger.error("获取目标服务实例ID出错", future.cause());
            }
        });
    }

    public void sendRPCResponse(IGameMessage gameMessage) {
        GameMessagePackage gameMessagePackage = new GameMessagePackage();
        gameMessagePackage.setHeader(gameMessage.getHeader());
        gameMessagePackage.setBody(gameMessage.body());
        // 创建响应消息的Topic
        String sendTopic = TopicUtil.generateTopic(responseTopic, gameMessage.getHeader().getToServerId());
        // 发送到消息总线服务中
        GameMessageInnerDecoder.sendMessage(kafkaTemplate, gameMessagePackage, sendTopic);
    }

    public void receiveResponse(IGameMessage gameMessage) {
        gameRpcCallbackService.callback(gameMessage);
    }
}
