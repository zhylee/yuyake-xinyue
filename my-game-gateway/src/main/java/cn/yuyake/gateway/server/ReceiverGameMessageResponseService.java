package cn.yuyake.gateway.server;

import cn.yuyake.game.bus.GameMessageInnerDecoder;
import cn.yuyake.game.common.GameMessagePackage;
import io.netty.channel.Channel;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 接收业务服务返回的消息，并发送到客户端
 */
@Service
public class ReceiverGameMessageResponseService {

    private final Logger logger = LoggerFactory.getLogger(ReceiverGameMessageResponseService.class);
    @Autowired
    private GatewayServerConfig gatewayServerConfig;
    @Autowired
    private ChannelService channelService;

    @PostConstruct
    public void init() {
        logger.info("监听消息接收业务消息topic：{}", gatewayServerConfig.getGatewayGameMessageTopic());
    }

    @KafkaListener(topics = {"${game.gateway.server.config.gateway-game-message-topic}"}, groupId = "${game.gateway.server.config.server-id}")
    public void receiver(ConsumerRecord<String, byte[]> record) {
        GameMessagePackage gameMessagePackage = GameMessageInnerDecoder.readGameMessagePackage(record.value());
        // 从包头中获取这个消息包归属的playerId
        Long playerId = gameMessagePackage.getHeader().getPlayerId();
        // 根据playerId找到这个客户端的连接Channel
        Channel channel = channelService.getChannel(playerId);
        if (channel != null) {
            // 给客户端返回消息
            channel.writeAndFlush(gameMessagePackage);
        }
    }
}
