package cn.yuyake.gateway.message.context;

import cn.yuyake.common.utils.TopicUtil;
import cn.yuyake.game.bus.GameMessageInnerDecoder;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.gateway.message.channel.GameChannelPromise;
import cn.yuyake.gateway.message.channel.IMessageSendFactory;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

public class GameGatewayMessageSendFactory implements IMessageSendFactory {

    private final String topic;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public GameGatewayMessageSendFactory(KafkaTemplate<String, byte[]> kafkaTemplate, String topic) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendMessage(GameMessagePackage gameMessagePackage, GameChannelPromise promise) {
        int toServerId = gameMessagePackage.getHeader().getToServerId();
        long playerId = gameMessagePackage.getHeader().getPlayerId();
        // 动态创建游戏网关监听消息的topic
        String sendTopic = TopicUtil.generateTopic(topic,toServerId);
        GameMessageInnerDecoder.sendMessage(kafkaTemplate, gameMessagePackage, sendTopic);
        promise.setSuccess();
    }
}
