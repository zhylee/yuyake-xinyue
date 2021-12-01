package cn.yuyake.xinyue.kafka;

import cn.yuyake.common.utils.TopicUtil;
import cn.yuyake.game.GameMessageService;
import cn.yuyake.game.bus.GameMessageInnerDecoder;
import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.game.message.xinyue.EnterGameMsgRequest;
import cn.yuyake.game.message.xinyue.EnterGameMsgResponse;
import cn.yuyake.xinyue.common.ServerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 此类主要用于学习和测试
 */
@Service
public class ReceiverGameMessageRequestService {
    private final Logger logger = LoggerFactory.getLogger(ReceiverGameMessageRequestService.class);

    @Autowired
    private ServerConfig serverConfig;
    @Autowired
    private GameMessageService gameMessageService;
    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    // 从 application.yml 中获取订阅的 topic 和 server-id
    @KafkaListener(topics = {"${game.server.config.business-game-message-topic}"}, groupId = "${game.server.config.server-id}")
    public void consume(ConsumerRecord<String, byte[]> record) {
        GameMessagePackage gameMessagePackage = GameMessageInnerDecoder.readGameMessagePackage(record.value());
        logger.debug("接收收网关消息：{}", gameMessagePackage.getHeader());
        GameMessageHeader header = gameMessagePackage.getHeader();
        // 如果此条消息的目标是这台服务器，则处理这条消息
        if (serverConfig.getServerId() == header.getToServerId()) {
            IGameMessage gameMessage = gameMessageService.getRequestInstanceByMessageId(header.getMessageId());
            if (gameMessage instanceof EnterGameMsgRequest) {
                // 给客户端返回消息，测试
                EnterGameMsgResponse response = new EnterGameMsgResponse();
                GameMessageHeader responseHeader = this.createResponseGameMessageHeader(header);
                response.setHeader(responseHeader);
                response.getBodyObj().setNickname("竹溪");
                response.getBodyObj().setPlayerId(header.getPlayerId());
                GameMessagePackage gameMessagePackage2 = new GameMessagePackage();
                gameMessagePackage2.setHeader(responseHeader);
                gameMessagePackage2.setBody(response.body());
                // 动态创建游戏网关监听消息的topic
                String topic = TopicUtil.generateTopic(serverConfig.getGatewayGameMessageTopic(), header.getFromServerId());
                GameMessageInnerDecoder.sendMessage(kafkaTemplate, gameMessagePackage2, topic);
            }
        }
    }

    /**
     * 根据请求的包头，创建响应的包头
     */
    private GameMessageHeader createResponseGameMessageHeader(GameMessageHeader requestGameMessageHeader) {
        GameMessageHeader newHeader = new GameMessageHeader();
        newHeader.setClientSendTime(requestGameMessageHeader.getClientSendTime());
        newHeader.setClientSeqId(requestGameMessageHeader.getClientSeqId());
        // 返回的消息中，消息来源的 serverId 就是接收消息时消息到达的 serverId
        newHeader.setFromServerId(requestGameMessageHeader.getToServerId());
        newHeader.setMessageId(requestGameMessageHeader.getMessageId());
        newHeader.setPlayerId(requestGameMessageHeader.getPlayerId());
        newHeader.setServerSendTime(System.currentTimeMillis());
        newHeader.setServiceId(requestGameMessageHeader.getServiceId());
        // 返回消息要到达的 serverId 就是接收消息的来源 serverId
        newHeader.setToServerId(requestGameMessageHeader.getFromServerId());
        newHeader.setVersion(requestGameMessageHeader.getVersion());
        return newHeader;
    }
}
