package cn.yuyake.client.service.handler;

import cn.yuyake.game.message.FirstMsgResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestGameMessageHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(TestGameMessageHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 判断消息类型
        if (msg instanceof FirstMsgResponse) {
            FirstMsgResponse response = (FirstMsgResponse) msg;
            logger.info("收到服务器响应：{}", response.getServerTime());
        }
    }
}
