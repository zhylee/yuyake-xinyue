package cn.yuyake.gateway.message.channel;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public interface GameChannelFuture extends Future<Void> {

    GameChannel channel();

    @Override
    Future<Void> addListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    Future<Void> addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    Future<Void> removeListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    Future<Void> removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    Future<Void> sync() throws InterruptedException;

    @Override
    Future<Void> syncUninterruptibly();

    @Override
    Future<Void> await() throws InterruptedException;

    @Override
    Future<Void> awaitUninterruptibly();
}
