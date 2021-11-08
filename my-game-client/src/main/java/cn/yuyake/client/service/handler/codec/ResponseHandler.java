package cn.yuyake.client.service.handler.codec;

import cn.yuyake.game.GameMessageService;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.game.common.IGameMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ResponseHandler extends ChannelInboundHandlerAdapter {

    private final GameMessageService gameMessageService;

    public ResponseHandler(GameMessageService gameMessageService) {
        this.gameMessageService = gameMessageService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        GameMessagePackage messagePackage = (GameMessagePackage) msg;
        int messageId = messagePackage.getHeader().getMessageId();
        // 根据 messageId 获取对应的对象实例
        IGameMessage gameMessage = gameMessageService.getResponseInstanceByMessageId(messageId);
        gameMessage.setHeader(messagePackage.getHeader());
        // 解析消息体
        gameMessage.read(messagePackage.getBody());
        // 下发到之后的 Handler
        ctx.fireChannelRead(gameMessage);
    }
}
