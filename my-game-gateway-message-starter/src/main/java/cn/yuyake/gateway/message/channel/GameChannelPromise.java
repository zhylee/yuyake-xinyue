package cn.yuyake.gateway.message.channel;

import io.netty.util.concurrent.Promise;

// TODO Game Channel Promise
public interface GameChannelPromise extends GameChannelFuture, Promise<Void> {

    @Override
    GameChannelPromise setSuccess(Void result);

    GameChannelPromise setSuccess();
}
