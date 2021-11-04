package cn.yuyake.client.service.handler.codec;

import cn.yuyake.client.service.GameClientConfig;
import cn.yuyake.game.common.IGameMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

// TODO Encode Handler
public class EncodeHandler extends MessageToByteEncoder<IGameMessage> {

    private GameClientConfig gameClientConfig;

    public EncodeHandler(GameClientConfig gameClientConfig) {
        this.gameClientConfig = gameClientConfig;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, IGameMessage msg, ByteBuf out) throws Exception {

    }
}
