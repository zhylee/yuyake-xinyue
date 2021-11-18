package cn.yuyake.client.service.handler.codec;

import cn.yuyake.client.service.GameClientConfig;
import cn.yuyake.common.utils.AESUtils;
import cn.yuyake.common.utils.CompressUtil;
import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.IGameMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 客户端编码类
 * <p>
 * 消息总长度(4)
 * + 客户端消息序列号长度(4)
 * + 消息请求ID长度（4）
 * + 服务ID(2)
 * + 客户端发送时间长度(8)
 * + 协议版本长度(4)
 * + 是否压缩长度(1)
 * </p>
 */
public class EncodeHandler extends MessageToByteEncoder<IGameMessage> {

    // 发送消息的包头总长度
    private static final int GAME_MESSAGE_HEADER_LEN = 27;
    private final GameClientConfig gameClientConfig;
    // 对称加密的密钥
    private String aesSecretKey;

    public EncodeHandler(GameClientConfig gameClientConfig) {
        this.gameClientConfig = gameClientConfig;
    }

    public void setAesSecretKey(String aesSecretKey) {
        this.aesSecretKey = aesSecretKey;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, IGameMessage msg, ByteBuf out) throws Exception {
        // 标记数据包的总大小
        int messageSize = GAME_MESSAGE_HEADER_LEN;
        byte[] body = msg.body();
        // 标记包体是否进行了压缩
        int compress = 0;
        if (body != null) {
            // 判断包体是否达到了需要压缩的值
            if (body.length >= gameClientConfig.getMessageCompressSize()) {
                // 包体大小达到需要压缩的值时，对包体进行压缩
                body = CompressUtil.compress(body);
                compress = 1;
            }
            if(this.aesSecretKey != null && msg.getHeader().getMessageId() != 1) {
                //密钥不为空，对消息体加密
                body = AESUtils.encode(aesSecretKey, body);
            }
            // 加上包体的长度，得到数据包的总大小
            messageSize += body.length;
        }
        GameMessageHeader header = msg.getHeader();
        out.writeInt(messageSize); // 依次写入包头数据
        out.writeInt(header.getClientSeqId());
        out.writeInt(header.getMessageId());
        out.writeShort(header.getServiceId());
        out.writeLong(header.getClientSendTime());
        out.writeInt(gameClientConfig.getVersion()); // 从配置中获取客户端版本
        out.writeByte(compress);
        // 如果包体不为空，写入包体数据
        if (body != null) {
            out.writeBytes(body);
        }
    }
}
