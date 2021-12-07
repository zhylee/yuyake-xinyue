package cn.yuyake.gateway.message.channel;

public interface GameChannelHandler {

    void exceptionCaught(AbstractGameChannelHandlerContext ctx, Throwable cause) throws Exception;
}
