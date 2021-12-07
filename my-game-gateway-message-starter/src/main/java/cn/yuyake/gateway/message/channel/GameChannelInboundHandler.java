package cn.yuyake.gateway.message.channel;

import io.netty.util.concurrent.Promise;

public interface GameChannelInboundHandler extends GameChannelHandler {
    //GameChannel第一次注册的时候调用
    void channelRegister(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise);

    //GameChannel被移除的时候调用
    void channelInactive(AbstractGameChannelHandlerContext ctx) throws Exception;

    //读取并处理客户端发送的消息
    void channelRead(AbstractGameChannelHandlerContext ctx, Object msg) throws Exception;

    //触发一些内部事件
    void userEventTriggered(AbstractGameChannelHandlerContext ctx, Object evt, Promise<Object> promise) throws Exception;
}
