package cn.yuyake.gateway.message.channel;

import io.netty.util.concurrent.EventExecutor;

public class DefaultGameChannelHandlerContext extends AbstractGameChannelHandlerContext {

    private final GameChannelHandler handler;

    public DefaultGameChannelHandlerContext(
            GameChannelPipeline pipeline,
            EventExecutor executor,
            String name,
            GameChannelHandler channelHandler) {
        super(pipeline, executor, name,
                // 判断这个channelHandler是处理接收消息的handler还是处理发出消息的handler
                channelHandler instanceof GameChannelInboundHandler,
                channelHandler instanceof GameChannelOutboundHandler);
        this.handler = channelHandler;
    }

    @Override
    public GameChannelHandler handler() {
        return handler;
    }
}
