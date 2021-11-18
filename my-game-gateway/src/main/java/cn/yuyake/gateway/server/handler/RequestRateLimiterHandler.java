package cn.yuyake.gateway.server.handler;

import cn.yuyake.game.common.GameMessagePackage;
import com.google.common.util.concurrent.RateLimiter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestRateLimiterHandler extends ChannelInboundHandlerAdapter {
    // 全局限制器
    private RateLimiter globalRateLimiter;
    // 用户限流器，用于限制单个用户的请求
    private static RateLimiter userRateLimiter;
    // 最后一次消息id
    private int lastClientSeqId = 0;
    private static final Logger logger = LoggerFactory.getLogger(RequestRateLimiterHandler.class);

    public RequestRateLimiterHandler(RateLimiter globalRateLimiter, double requestPerSecond) {
        this.globalRateLimiter = globalRateLimiter;
        userRateLimiter = RateLimiter.create(requestPerSecond);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 获取令牌失败，触发限流
        if (!userRateLimiter.tryAcquire()) {
            logger.debug("channel {} 请求过多，连接断开", ctx.channel().id().asShortText());
            ctx.close();
            return;
        }
        // 获取全局令牌失败，触发限流
        if (!globalRateLimiter.tryAcquire()) {
            logger.debug("全局请求超载，channel {} 断开", ctx.channel().id().asShortText());
            ctx.close();
            return;
        }
        // 消息幂等处理
        GameMessagePackage gameMessagePackage = (GameMessagePackage) msg;
        int clientSeqId = gameMessagePackage.getHeader().getClientSeqId();
        if (lastClientSeqId > 0) {
            if (clientSeqId <= lastClientSeqId) {
                return;
            }
        }
        // 记录本次处理的消息序列号
        this.lastClientSeqId = clientSeqId;
        // 不要忘记添加这个，要不然后面的handler收不到消息
        ctx.fireChannelRead(msg);
    }
}
