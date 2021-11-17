package cn.yuyake.gateway.server.handler;

import cn.yuyake.common.utils.JWTUtil.TokenBody;
import cn.yuyake.gateway.server.GatewayServerConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 处理连接检测与认证
 */
public class ConfirmHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(ConfirmHandler.class);
    // 注入服务器配置
    private GatewayServerConfig serverConfig;
    // 标记连接是否认证成功
    private boolean confirmSuccess = false;
    // 定时器的返回值
    private ScheduledFuture<?> future;
    // token body
    private TokenBody tokenBody;

    public ConfirmHandler(GatewayServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    /**
     * 此方法会在连接建立成功 channel 注册之后调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 从配置中获取延迟时间
        int delay = serverConfig.getWaitConfirmTimeoutSecond();
        // 添加延时定时器
        future = ctx.channel().eventLoop().schedule(() -> {
            // 如果没有认证成功，则关闭
            if (!confirmSuccess) {
                ctx.close();
            }
        }, delay, TimeUnit.SECONDS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (future != null) {
            // 如果连接关闭了，取消定时检测任务
            future.cancel(true);
            // 接下来告诉下面的 Handler
            ctx.fireChannelInactive();
        }
    }
}
