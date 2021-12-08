package cn.yuyake.gateway.message.handler;

import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.gateway.message.channel.AbstractGameChannelHandlerContext;
import cn.yuyake.gateway.message.channel.GameChannelInboundHandler;
import cn.yuyake.gateway.message.channel.GameChannelOutboundHandler;
import cn.yuyake.gateway.message.channel.GameChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameChannelIdleStateHandler implements GameChannelInboundHandler, GameChannelOutboundHandler {
    // 延迟事件的延迟时间的最小值
    private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
    // 读取消息的空闲时间，单位纳秒
    private final long readerIdleTimeNanos;
    // 写出消息的空闲时间，单位纳秒
    private final long writerIdleTimeNanos;
    // 读取和写出消息的空闲时间，单位纳秒
    private final long allIdleTimeNanos;
    // 读取消息的超时延时检测事件
    private ScheduledFuture<?> readerIdleTimeout;
    // 最近一次读取消息的时间
    private long lastReadTime;
    // 写出消息的超时延时检测事件
    private ScheduledFuture<?> writerIdleTimeout;
    // 最近一次写出消息的时间
    private long lastWriteTime;
    // 读写消息的超时检测事件
    private ScheduledFuture<?> allIdleTimeout;
    // 0 - none, 1 - initialized, 2 - destroyed
    private byte state;

    public GameChannelIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS);
    }

    public GameChannelIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        if (readerIdleTime <= 0) {
            readerIdleTimeNanos = 0;
        } else {
            readerIdleTimeNanos = Math.max(unit.toNanos(readerIdleTime), MIN_TIMEOUT_NANOS);
        }
        if (writerIdleTime <= 0) {
            writerIdleTimeNanos = 0;
        } else {
            writerIdleTimeNanos = Math.max(unit.toNanos(writerIdleTime), MIN_TIMEOUT_NANOS);
        }
        if (allIdleTime <= 0) {
            allIdleTimeNanos = 0;
        } else {
            allIdleTimeNanos = Math.max(unit.toNanos(allIdleTime), MIN_TIMEOUT_NANOS);
        }
    }

    // 获取空闲事件类型
    protected IdleStateEvent newIdleStateEvent(IdleState state) {
        switch (state) {
            case ALL_IDLE:
                // 读取、写出消息超时事件
                return IdleStateEvent.ALL_IDLE_STATE_EVENT;
            case READER_IDLE:
                // 读取消息超时事件
                return IdleStateEvent.READER_IDLE_STATE_EVENT;
            case WRITER_IDLE:
                // 写出消息超时事件
                return IdleStateEvent.WRITER_IDLE_STATE_EVENT;
            default:
                throw new IllegalArgumentException("Unhandled: state=" + state);
        }
    }

    private void initialize(AbstractGameChannelHandlerContext ctx) {
        switch (state) {
            case 1:
            case 2:
                return;
        }
        state = 1;
        lastReadTime = lastWriteTime = ticksInNanos();
        if (readerIdleTimeNanos > 0) {
            // 初始化创建读取消息事件检测延时任务
            readerIdleTimeout = schedule(ctx, new ReaderIdleTimeoutTask(ctx), readerIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
        if (writerIdleTimeNanos > 0) {
            // 初始化创建写出消息事件检测延时任务
            writerIdleTimeout = schedule(ctx, new WriterIdleTimeoutTask(ctx), writerIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
        if (allIdleTimeNanos > 0) {
            // 初始化创建读取和写出消息事件检测延时任务
            allIdleTimeout = schedule(ctx, new AllIdleTimeoutTask(ctx), allIdleTimeNanos, TimeUnit.NANOSECONDS);
        }
    }

    // 销毁定时事件任务
    private void destroy() {
        state = 2;
        if (readerIdleTimeout != null) {
            readerIdleTimeout.cancel(false);
            readerIdleTimeout = null;
        }
        if (writerIdleTimeout != null) {
            writerIdleTimeout.cancel(false);
            writerIdleTimeout = null;
        }
        if (allIdleTimeout != null) {
            allIdleTimeout.cancel(false);
            allIdleTimeout = null;
        }
    }

    @Override
    public void exceptionCaught(AbstractGameChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void channelRegister(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise) {
        initialize(ctx);
        ctx.fireChannelRegistered(playerId, promise);
    }

    @Override
    public void channelInactive(AbstractGameChannelHandlerContext ctx) throws Exception {
        destroy();
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(AbstractGameChannelHandlerContext ctx, Object msg) throws Exception {
        if (readerIdleTimeNanos > 0 || allIdleTimeNanos > 0) {
            // 记录最后一次读取操作的时间
            this.lastReadTime = this.ticksInNanos();
        }
        // 注意，这句一定不能少，要不然后面的Handler就收不到消息了
        ctx.fireChannelRead(msg);
    }

    @Override
    public void userEventTriggered(AbstractGameChannelHandlerContext ctx, Object evt, Promise<Object> promise) throws Exception {
        ctx.fireUserEventTriggered(evt, promise);
    }

    @Override
    public void writeAndFlush(AbstractGameChannelHandlerContext ctx, IGameMessage msg, GameChannelPromise promise) throws Exception {
        if (writerIdleTimeNanos > 0 || allIdleTimeNanos > 0) {
            this.lastWriteTime = this.ticksInNanos();
        }
        // 注意，这句不能少，少了的话消息会发不出去
        ctx.writeAndFlush(msg, promise);
    }

    @Override
    public void close(AbstractGameChannelHandlerContext ctx, GameChannelPromise promise) {
        ctx.close(promise);
    }


    // 获取当前时间的纳秒
    private long ticksInNanos() {
        return System.nanoTime();
    }

    // 创建延时任务
    private ScheduledFuture<?> schedule(AbstractGameChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit) {
        return ctx.executor().schedule(task, delay, unit);
    }

    // 发送空闲事件
    private void channelIdle(AbstractGameChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        ctx.fireUserEventTriggered(evt, null);
    }

    // 公共抽象任务
    private abstract static class AbstractIdleTask implements Runnable {
        private final AbstractGameChannelHandlerContext ctx;

        AbstractIdleTask(AbstractGameChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!ctx.gameChannel().isRegistered()) {
                return;
            }
            run(ctx);
        }

        protected abstract void run(AbstractGameChannelHandlerContext ctx);
    }

    // 读取消息检测任务
    private final class ReaderIdleTimeoutTask extends AbstractIdleTask {

        ReaderIdleTimeoutTask(AbstractGameChannelHandlerContext ctx) {
            super(ctx);
        }

        @Override
        protected void run(AbstractGameChannelHandlerContext ctx) {
            long nextDelay = readerIdleTimeNanos;
            nextDelay -= ticksInNanos() - lastReadTime;
            if (nextDelay <= 0) {
                // 说明读取事件超时，发送空闲事件，并创建新的延迟任务，用于下次超时检测
                // 启动新的检测事件
                readerIdleTimeout = schedule(ctx, this, readerIdleTimeNanos, TimeUnit.NANOSECONDS);
                try {
                    IdleStateEvent event = newIdleStateEvent(IdleState.READER_IDLE);
                    // 向Channel中发送超时事件
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // 没有超时，从上次读取的时间起，计时计算下次超时检测
                // 重新启动检测事件
                readerIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }

    private final class WriterIdleTimeoutTask extends AbstractIdleTask {

        WriterIdleTimeoutTask(AbstractGameChannelHandlerContext ctx) {
            super(ctx);
        }

        @Override
        protected void run(AbstractGameChannelHandlerContext ctx) {
            long lastWriteTime = GameChannelIdleStateHandler.this.lastWriteTime;
            long nextDelay = writerIdleTimeNanos - (ticksInNanos() - lastWriteTime);
            if (nextDelay <= 0) {
                // Writer is idle - set a new timeout and notify the callback.
                writerIdleTimeout = schedule(ctx, this, writerIdleTimeNanos, TimeUnit.NANOSECONDS);
                try {
                    IdleStateEvent event = newIdleStateEvent(IdleState.WRITER_IDLE);
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                writerIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }

    private final class AllIdleTimeoutTask extends AbstractIdleTask {

        AllIdleTimeoutTask(AbstractGameChannelHandlerContext ctx) {
            super(ctx);
        }

        @Override
        protected void run(AbstractGameChannelHandlerContext ctx) {
            long nextDelay = allIdleTimeNanos;
            nextDelay -= ticksInNanos() - Math.max(lastReadTime, lastWriteTime);
            if (nextDelay <= 0) {
                // Both reader and writer are idle - set a new timeout and
                // notify the callback.
                allIdleTimeout = schedule(ctx, this, allIdleTimeNanos, TimeUnit.NANOSECONDS);
                try {
                    IdleStateEvent event = newIdleStateEvent(IdleState.ALL_IDLE);
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                allIdleTimeout = schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }
}
