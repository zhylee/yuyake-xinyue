package cn.yuyake.gateway.server.handler;

import cn.yuyake.game.GameMessageService;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.game.message.*;
import cn.yuyake.game.message.body.ThirdMsgBody;
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
            logger.debug("收到 request 1：{}", request.getValue());
            FirstMsgResponse response = new FirstMsgResponse();
            response.setServerTime(System.currentTimeMillis());
            // 给客户端回消息
            GameMessagePackage returnPackage = new GameMessagePackage();
            returnPackage.setHeader(response.getHeader());
            returnPackage.setBody(response.body());
            ctx.writeAndFlush(returnPackage);
        } else if (messageId == 10002) {
            SecondMsgRequest request = (SecondMsgRequest) messageService.getRequestInstanceByMessageId(messageId);
            request.read(gameMessagePackage.getBody());
            logger.debug("收到 request 2：{}", request);
            SecondMsgResponse response = new SecondMsgResponse();
            response.getBodyObj().setResult1(System.currentTimeMillis());
            response.getBodyObj().setResult2("服务器回复");
            GameMessagePackage returnPackage = new GameMessagePackage();
            returnPackage.setHeader(response.getHeader());
            returnPackage.setBody(response.body());
            ctx.writeAndFlush(returnPackage);
        } else if (messageId == 10003) {
            ThirdMsgRequest request = (ThirdMsgRequest) messageService.getRequestInstanceByMessageId(messageId);
            // 反序列化客户端的请求消息
            request.read(gameMessagePackage.getBody());
            logger.debug("收到 request 3：{}", request.getRequestBody().getValue1());
            // 构造服务器响应的对应
            ThirdMsgResponse response = new ThirdMsgResponse();
            ThirdMsgBody.ThirdMsgResponseBody responseBody = ThirdMsgBody.ThirdMsgResponseBody.newBuilder()
                    .setValue1("服务器收到protobuf")
                    .setValue2(3)
                    .setValue3("服务器返回")
                    .build();
            // 设置服务器返回的数据
            response.setResponseBody(responseBody);
            GameMessagePackage returnPackage = new GameMessagePackage();
            returnPackage.setHeader(response.getHeader());
            returnPackage.setBody(response.body());
            ctx.writeAndFlush(returnPackage);
        }
    }
}
