package cn.yuyake.gateway.server.handler;

import cn.yuyake.game.GameMessageService;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.game.message.FirstMsgRequest;
import cn.yuyake.game.message.FirstMsgResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试接收客户端发送的请求，并给客户端返回数据
 */
public class TestGameMessageHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(TestGameMessageHandler.class);
    private final GameMessageService messageService;

    public TestGameMessageHandler(GameMessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        GameMessagePackage gameMessagePackage = (GameMessagePackage) msg;
        int messageId = gameMessagePackage.getHeader().getMessageId();
        // 根据消息号处理不同的请求业务
        if (messageId == 10001) {
            FirstMsgRequest request = new FirstMsgRequest();
            // 读取消息体
            request.read(gameMessagePackage.getBody());
            logger.debug("接收到客户端消息：{}", request.getValue());
            FirstMsgResponse response = new FirstMsgResponse();
            response.setServerTime(System.currentTimeMillis());
            // 给客户端回消息
            GameMessagePackage returnPackage = new GameMessagePackage();
            returnPackage.setHeader(response.getHeader());
            returnPackage.setBody(response.body());
            ctx.writeAndFlush(returnPackage);
        }
    }
}
