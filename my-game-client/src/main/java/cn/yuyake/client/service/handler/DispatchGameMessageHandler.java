package cn.yuyake.client.service.handler;

import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.game.messagedispatcher.DispatchGameMessageService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 接收服务器响应的消息，并将消息分发到业务处理方法中
 */
public class DispatchGameMessageHandler extends ChannelInboundHandlerAdapter {
    private DispatchGameMessageService dispatchGameMessageService;
    private static Logger logger = LoggerFactory.getLogger(DispatchGameMessageHandler.class);
    public DispatchGameMessageHandler(DispatchGameMessageService dispatchGameMessageService) {
        this.dispatchGameMessageService = dispatchGameMessageService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        IGameMessage gameMessage = (IGameMessage) msg;
        // 构造消息处理的上下文
        GameClientChannelContext gameClientChannelContext = new GameClientChannelContext(ctx.channel(), gameMessage);
        dispatchGameMessageService.callMethod(gameMessage, gameClientChannelContext);
    }
}
