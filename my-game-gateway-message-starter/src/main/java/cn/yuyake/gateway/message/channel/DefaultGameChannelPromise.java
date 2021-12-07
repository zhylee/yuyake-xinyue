package cn.yuyake.gateway.message.channel;

import io.netty.util.concurrent.DefaultPromise;

// TODO Default Game Channel Promise
public class DefaultGameChannelPromise extends DefaultPromise<Void> implements GameChannelPromise {
    private final GameChannel channel;

    public DefaultGameChannelPromise(GameChannel channel) {
        this.channel = channel;
    }
}
