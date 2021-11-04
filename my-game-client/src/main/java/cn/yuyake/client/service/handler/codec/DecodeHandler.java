package cn.yuyake.client.service.handler.codec;

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
 * 客户端解码类
 */
public class DecodeHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(DecodeHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 一个完整的数据包
        ByteBuf buf = (ByteBuf) msg;

        try {
            // 根据协议，依次读取包头的信息
            int messageSize = buf.readInt();
            int clientSeqId = buf.readInt();
            int messageId = buf.readInt();
            long serverSendTime = buf.readLong();
            int version = buf.readInt();
            int compress = buf.readByte();
            int errorCode = buf.readInt();
            byte[] body = null;
            if (errorCode == 0 && buf.readableBytes() > 0) {
                // 读取包体数据
                body = new byte[buf.readableBytes()];
                // 剩下的字节都是body数据
                buf.readBytes(body);
                // 如果包体压缩了，接收时需要解压
                if (compress == 1) {
                    body = CompressUtil.decompress(body);
                }
            }
            GameMessageHeader header = new GameMessageHeader();
            header.setClientSeqId(clientSeqId);
            header.setErrorCode(errorCode);
            header.setMessageId(messageId);
            header.setServerSendTime(serverSendTime);
            header.setVersion(version);
            header.setMessageSize(messageSize);
            // 构造数据包
            GameMessagePackage gameMessagePackage = new GameMessagePackage();
            gameMessagePackage.setHeader(header);
            gameMessagePackage.setBody(body);
            logger.debug("接收服务器消息,大小：{}:<-{}", messageSize, header);
            // 将解码出来的消息发送到后面的Handler
            ctx.fireChannelRead(gameMessagePackage);
        } catch (IOException e) {
            logger.error("解压缩错误", e);
        } finally {
            // 这里做了判断，如果buf不是从堆内存分配，还是从直接内存中分配的，需要手动释放
            // 否则，会造成内存泄露
            ReferenceCountUtil.release(buf);
        }

    }
}
