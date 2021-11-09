package cn.yuyake.client.service.handler;

import cn.yuyake.game.message.FirstMsgResponse;
import cn.yuyake.game.message.SecondMsgResponse;
import cn.yuyake.game.message.ThirdMsgResponse;
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
            logger.info("first msg response: {}", response.getServerTime());
        }
        if (msg instanceof SecondMsgResponse) {
            SecondMsgResponse response = (SecondMsgResponse) msg;
            logger.info("second msg response: {}", response.getBodyObj().getResult1());
        }
        if(msg instanceof ThirdMsgResponse) {
            ThirdMsgResponse response = (ThirdMsgResponse)msg;
            logger.info("third msg response: {}",response.getResponseBody().getValue1());
        }
    }
}
