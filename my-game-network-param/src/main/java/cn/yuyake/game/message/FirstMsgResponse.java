package cn.yuyake.game.message;

import cn.yuyake.game.common.AbstractGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 响应数据对象
 */
@GameMessageMetadata(messageId = 10001, messageType = EnumMessageType.RESPONSE, serviceId = 1) // 添加元数据信息
public class FirstMsgResponse extends AbstractGameMessage {

    private Long serverTime; // 返回服务器的时间

    @Override
    protected byte[] encode() {
        ByteBuf byteBuf = Unpooled.buffer(8);
        byteBuf.writeLong(serverTime);
        return byteBuf.array();
    }

    @Override
    protected void decode(byte[] body) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(body);
        this.serverTime = byteBuf.readLong();
    }

    @Override
    protected boolean isBodyMsgNull() {
        return this.serverTime == null;
    }

    public Long getServerTime() {
        return serverTime;
    }

    public void setServerTime(Long serverTime) {
        this.serverTime = serverTime;
    }
}
