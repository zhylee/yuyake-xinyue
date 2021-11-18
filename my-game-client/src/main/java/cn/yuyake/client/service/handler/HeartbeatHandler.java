package cn.yuyake.client.service.handler;

import cn.yuyake.game.message.HeartbeatMsgRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    // 标记连接是否认证成功
    private boolean confirmSuccess;

    // 在连接认证成功的方法中调用此方法，标记连接认证成功
    public void setConfirmSuccess(boolean confirmSuccess) {
        this.confirmSuccess = confirmSuccess;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            // 接收写出空闲事件，说明一定时间内没有向服务器发送消息了
            if (event.state() == IdleState.WRITER_IDLE) {
                // 连接认证成功之后再发送
                if (confirmSuccess) {
                    // 发送心跳事件
                    HeartbeatMsgRequest request = new HeartbeatMsgRequest();
                    ctx.writeAndFlush(request);
                }
            }
        }
    }
}
