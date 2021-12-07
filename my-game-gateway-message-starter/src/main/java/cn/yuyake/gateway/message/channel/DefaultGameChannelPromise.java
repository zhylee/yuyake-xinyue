package cn.yuyake.gateway.message.channel;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class DefaultGameChannelPromise extends DefaultPromise<Void> implements GameChannelPromise {
    private final GameChannel channel;

    public DefaultGameChannelPromise(GameChannel channel) {
        this.channel = channel;
    }

    public DefaultGameChannelPromise(GameChannel channel, EventExecutor executor) {
        super(executor);
        this.channel = channel;
    }

    @Override
    public GameChannel channel() {
        return channel;
    }

    @Override
    protected EventExecutor executor() {
        EventExecutor e = super.executor();
        if (e == null) {
            return channel().executor();
        } else {
            return e;
        }
    }

    @Override
    public GameChannelPromise setSuccess(Void result) {
        super.setSuccess(result);
        return this;
    }

    @Override
    public GameChannelPromise setSuccess() {
        return setSuccess(null);
    }

    @Override
    public GameChannelPromise setFailure(Throwable cause) {
        super.setFailure(cause);
        return this;
    }

    @Override
    public GameChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.addListener(listener);
        return this;
    }

    @SafeVarargs
    @Override
    public final GameChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.addListeners(listeners);
        return this;
    }

    @Override
    public GameChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.removeListener(listener);
        return this;
    }

    @SafeVarargs
    @Override
    public final GameChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.removeListeners(listeners);
        return this;
    }

    @Override
    public GameChannelPromise sync() throws InterruptedException {
        super.sync();
        return this;
    }

    @Override
    public GameChannelPromise syncUninterruptibly() {
        super.syncUninterruptibly();
        return this;
    }

    @Override
    public GameChannelPromise await() throws InterruptedException {
        super.await();
        return this;
    }

    @Override
    public GameChannelPromise awaitUninterruptibly() {
        super.awaitUninterruptibly();
        return this;
    }

    @Override
    protected void checkDeadLock() {
        super.checkDeadLock();
    }
}
