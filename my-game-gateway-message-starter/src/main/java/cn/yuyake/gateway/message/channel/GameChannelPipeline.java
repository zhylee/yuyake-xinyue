package cn.yuyake.gateway.message.channel;

import cn.yuyake.game.common.IGameMessage;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

// TODO Game Channel Pipeline
public class GameChannelPipeline {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultChannelPipeline.class);

    private final GameChannel channel;

    public GameChannelPipeline(GameChannel channel) {
        this.channel = ObjectUtil.checkNotNull(channel, "channel");
    }

    public final GameChannel getChannel() {
        return channel;
    }

    public final GameChannelPipeline fireRegister(long playerId, GameChannelPromise promise) {
        return null;
    }

    public final GameChannelPipeline fireChannelInactive() {
        return null;
    }

    public final GameChannelPipeline fireChannelRead(Object msg) {
        return null;
    }

    public final GameChannelPipeline fireUserEventTriggered(Object event, Promise<Object> promise) {
        return null;
    }

    public final GameChannelFuture writeAndFlush(IGameMessage msg) {
        return null;
    }
}
