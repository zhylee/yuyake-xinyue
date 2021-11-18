package cn.yuyake.gateway.server.handler.codec;

import cn.yuyake.common.utils.AESUtils;
import cn.yuyake.common.utils.CompressUtil;
import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.GameMessagePackage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 解码客户端消息
 * <p>
 *     协议格式为：消息总长度(int 4)
 *     + 长度检验码(int 4)
 *     + 消息序列号(int 4)
 *     + 消息号(int 4)
 *     + 服务ID（2）
 *     + 客户端发送时间(long 8)
 *     + 版本号(int 4)
 *     + 是否压缩(byte 1)
 *     + body（变长）
 * </p>
 */
public class DecodeHandler extends ChannelInboundHandlerAdapter {
    // 对称加密密钥
    private String aesSecret;

    public void setAesSecret(String aesSecret) {
        this.aesSecret = aesSecret;
    }

    private final static Logger logger = LoggerFactory.getLogger(DecodeHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        try {
            // 依次读取各个字段的数据
            int messageSize = byteBuf.readInt();
            // 读取客户端序列号
            int clientSeqId = byteBuf.readInt();
            // 读取消息号
            int messageId = byteBuf.readInt();
            // 读取服务ID
            int serviceId = byteBuf.readShort();
            // 读取客户端发送时间
            long clientSendTime = byteBuf.readLong();
            // 读取版本号
            int version = byteBuf.readInt();
            // 读取是否压缩数据的判断
            int compress = byteBuf.readByte();
            byte[] body = null;
            if (byteBuf.readableBytes() > 0) {
                body = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(body);
                // 如果密钥不为空，且不是认证消息，对消息体解密
                if (this.aesSecret != null && messageId != 1) {
                    body = AESUtils.decode(aesSecret, body);
                }
                if (compress == 1) {
                    // 如果压缩过，进行解压
                    body = CompressUtil.decompress(body);
                }
            }
            GameMessageHeader header = new GameMessageHeader();
            header.setClientSendTime(clientSendTime);
            header.setClientSeqId(clientSeqId);
            header.setMessageId(messageId);
            header.setServiceId(serviceId);
            header.setMessageSize(messageSize);
            header.setVersion(version);
            GameMessagePackage gameMessagePackage = new GameMessagePackage();
            gameMessagePackage.setHeader(header);
            gameMessagePackage.setBody(body);
            ctx.fireChannelRead(gameMessagePackage);
        } catch (IOException e) {
            logger.error("解压缩错误", e);
        } finally {
            // 一定要判断是否引用类 byteBuf，如果是进行释放
            ReferenceCountUtil.release(byteBuf);
        }
    }
}
