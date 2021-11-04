package cn.yuyake.client.service.handler.codec;

import cn.yuyake.game.GameMessageService;
import io.netty.channel.ChannelInboundHandlerAdapter;

// TODO Response Handler
public class ResponseHandler extends ChannelInboundHandlerAdapter {

    private GameMessageService gameMessageService;

    public ResponseHandler(GameMessageService gameMessageService) {
        this.gameMessageService = gameMessageService;
    }
}
