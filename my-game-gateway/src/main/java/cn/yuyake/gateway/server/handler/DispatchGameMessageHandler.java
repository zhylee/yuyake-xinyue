package cn.yuyake.gateway.server.handler;

import cn.yuyake.common.cloud.PlayerServiceInstance;
import cn.yuyake.common.utils.JWTUtil.TokenBody;
import cn.yuyake.game.bus.GameMessageInnerDecoder;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.gateway.server.GatewayServerConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.net.InetSocketAddress;

public class DispatchGameMessageHandler extends ChannelInboundHandlerAdapter {
    private TokenBody tokenBody;
    // 注入业务服务管理类，从这里获取负载均衡的服务器信息
    private final PlayerServiceInstance playerServiceInstance;
    // 注入游戏网关服务配置信息
    private final GatewayServerConfig gatewayServerConfig;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(DispatchGameMessageHandler.class);

    public DispatchGameMessageHandler(KafkaTemplate<String, byte[]> kafkaTemplate, PlayerServiceInstance playerServiceInstance, GatewayServerConfig gatewayServerConfig) {
        this.playerServiceInstance = playerServiceInstance;
        this.gatewayServerConfig = gatewayServerConfig;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        GameMessagePackage gameMessagePackage = (GameMessagePackage) msg;
        int serviceId = gameMessagePackage.getHeader().getServiceId();
        // 如果首次通信，获取验证信息
        if (tokenBody == null) {
            ConfirmHandler confirmHandler = (ConfirmHandler) ctx.channel().pipeline().get("ConfirmHandler");
            tokenBody = confirmHandler.getTokenBody();
        }
        InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = ipSocket.getAddress().getHostAddress();
        dispatchMessage(kafkaTemplate, ctx.executor(), playerServiceInstance, tokenBody.getPlayerId(), serviceId, clientIp, gameMessagePackage, gatewayServerConfig);
    }

    public static void dispatchMessage(
            KafkaTemplate<String, byte[]> kafkaTemplate,
            EventExecutor executor,
            PlayerServiceInstance playerServiceInstance,
            long playerId,
            int serviceId,
            String clientIp,
            GameMessagePackage gameMessagePackage,
            GatewayServerConfig gatewayServerConfig) {
        Promise<Integer> promise = new DefaultPromise<>(executor);
        playerServiceInstance.selectServerId(playerId, serviceId, promise).addListener((GenericFutureListener<Future<Integer>>) future -> {
            // 从多个服务实例中，选择一个合适的服务ID
            if (future.isSuccess()) {
                Integer toServerId = future.get();
                gameMessagePackage.getHeader().setToServerId(toServerId);
                gameMessagePackage.getHeader().setFromServerId(gatewayServerConfig.getServerId());
                gameMessagePackage.getHeader().getAttribute().setClientIp(clientIp);
                gameMessagePackage.getHeader().setPlayerId(playerId);
                // 动态创建与业务服务交互的消息总线Topic
                String topic = gatewayServerConfig.getBusinessGameMessageTopic() + toServerId;
                // 向消息总线服务发布客户端请求消息
                GameMessageInnerDecoder.sendMessage(kafkaTemplate, gameMessagePackage, topic);
                logger.debug("发送消息成功->{}", gameMessagePackage.getHeader());
            } else {
                logger.error("消息发送失败", future.cause());
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        logger.error("服务器异常，连接{}断开", ctx.channel().id().asShortText(), cause);
    }
}
