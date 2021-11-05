package cn.yuyake.gateway.server.handler.codec;

import cn.yuyake.common.utils.CompressUtil;
import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.gateway.server.GatewayServerConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码服务器向客户端发送的数据
 */
public class EncodeHandler extends MessageToByteEncoder<GameMessagePackage> {
    private static final int GAME_MESSAGE_HEADER_LEN = 29;
    private final GatewayServerConfig serverConfig;

    public EncodeHandler(GatewayServerConfig serverConfig) {
        // 注入服务端配置
        this.serverConfig = serverConfig;
    }
    @Override
    protected void encode(ChannelHandlerContext ctx, GameMessagePackage msg, ByteBuf out) throws Exception {
        int messageSize = GAME_MESSAGE_HEADER_LEN;
        byte[] body = msg.getBody();
        int compress = 0;
        if (body != null) {
            if (body.length >= serverConfig.getCompressMessageSize()) {
                // 达到压缩条件，进行压缩
                body = CompressUtil.compress(body);
                compress = 1;
            }
            messageSize += body.length;
        }
        out.writeInt(messageSize);
        GameMessageHeader header = msg.getHeader();
        out.writeInt(header.getClientSeqId());
        out.writeInt(header.getMessageId());
        out.writeLong(header.getServerSendTime());
        out.writeInt(header.getVersion());
        out.writeByte(compress);
        out.writeInt(header.getErrorCode());
        if (body != null) {
            out.writeBytes(body);
        }
    }
}
