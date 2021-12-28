package cn.yuyake.im.logic;

import cn.yuyake.db.entity.manager.IMManager;
import cn.yuyake.game.message.im.CrossSendIMMsgRequest;
import cn.yuyake.game.message.im.CrossSendIMMsgResponse;
import cn.yuyake.game.messagedispatcher.GameMessageHandler;
import cn.yuyake.game.messagedispatcher.GameMessageMapping;
import cn.yuyake.gateway.message.context.GatewayMessageConsumerService;
import cn.yuyake.gateway.message.context.GatewayMessageContext;
import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.UnsupportedEncodingException;

@GameMessageHandler
public class IMLogicHandler {
    private final static String IM_TOPIC = "game-im-topic";
    private final static String CHARSET_NAME = "utf8";
    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;
    @Autowired
    private GatewayMessageConsumerService gatewayMessageConsumerService;

    // 发布消息Kafka服务之中
    private void publishMessage(ChatMessage chatMessage) {
        String json = JSON.toJSONString(chatMessage);
        try {
            byte[] message = json.getBytes(CHARSET_NAME);
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(IM_TOPIC, "IM", message);
            kafkaTemplate.send(record);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    // 这里需要注意的是groupId一定要不一样，因为kafka的机制是一个消息只能被同一个消费者组下的某个消费者消费一次。不同的服务实例的serverId不一样
    @KafkaListener(topics = {IM_TOPIC}, groupId = "IM-SERVER-" + "${game.server.config.server-id}")
    public void messageListener(ConsumerRecord<String, byte[]> record) {
        // 监听聊天服务发布的信息，收到信息之后，将聊天信息转发到所有的客户端。
        byte[] value = record.value();
        try {
            String json = new String(value, CHARSET_NAME);
            ChatMessage chatMessage = JSON.parseObject(json, ChatMessage.class);
            CrossSendIMMsgResponse response = new CrossSendIMMsgResponse();
            response.getBodyObj().setChat(chatMessage.getChatMessage());
            response.getBodyObj().setSender(chatMessage.getNickName());
            // 因为这里不再GatewayMessageContext参数，所以这里使用总的GameChannel管理类，将消息广播出去
            gatewayMessageConsumerService.getGameMessageEventDispatchService().broadcastMessage(response);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @GameMessageMapping(CrossSendIMMsgRequest.class) // 在这里接收客户端发送的聊天消息
    public void chatMsg(CrossSendIMMsgRequest request, GatewayMessageContext<IMManager> ctx) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatMessage(request.getBodyObj().getChat());
        chatMessage.setNickName(request.getBodyObj().getSender());
        chatMessage.setPlayerId(ctx.getPlayerId());
        // 收到客户端的聊天消息之后，把消息封装，发布到Kafka之中
        this.publishMessage(chatMessage);
    }
}
