package cn.yuyake.gateway.message.channel;

import cn.yuyake.game.common.IGameMessage;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

// TODO Abstract Game Channel Handler Context
public abstract class AbstractGameChannelHandlerContext {
    static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultChannelPipeline.class);

    volatile AbstractGameChannelHandlerContext next;
    volatile AbstractGameChannelHandlerContext prev;
    private final String name;
    private final GameChannelPipeline pipeline;
    private final boolean inbound;
    private final boolean outbound;
    final EventExecutor executor;

    public AbstractGameChannelHandlerContext(GameChannelPipeline pipeline, EventExecutor executor, String name, boolean inbound, boolean outbound) {

        this.name = ObjectUtil.checkNotNull(name, "name");
        this.pipeline = pipeline;
        this.executor = executor;
        this.inbound = inbound;
        this.outbound = outbound;

    }

    static void invokeChannelRegistered(final AbstractGameChannelHandlerContext next, long playerId, GameChannelPromise promise) {

    }

    static void invokeChannelInactive(final AbstractGameChannelHandlerContext next) {

    }

    static void invokeExceptionCaught(final AbstractGameChannelHandlerContext next, final Throwable cause) {

    }

    static void invokeUserEventTriggered(final AbstractGameChannelHandlerContext next, final Object event, Promise<Object> promise) {

    }

    static void invokeChannelRead(final AbstractGameChannelHandlerContext next, final Object msg) {

    }

    public GameChannelFuture writeAndFlush(IGameMessage msg, GameChannelPromise promise) {
        return null;
    }

    public GameChannelFuture writeAndFlush(IGameMessage msg) {
        return null;
    }

    public AbstractGameChannelHandlerContext fireExceptionCaught(final Throwable cause) {
        return null;
    }

    public AbstractGameChannelHandlerContext fireChannelInactive() {
        return null;
    }

    public AbstractGameChannelHandlerContext fireChannelRead(final Object msg) {
        return null;
    }

    public AbstractGameChannelHandlerContext fireUserEventTriggered(final Object event, Promise<Object> promise) {
        return null;
    }

    public AbstractGameChannelHandlerContext fireChannelRegistered(long playerId, GameChannelPromise promise) {
        return null;
    }

    public AbstractGameChannelHandlerContext fireChannelReadRPCRequest(final IGameMessage msg) {
        return null;
    }


    public abstract GameChannelHandler handler();
}
