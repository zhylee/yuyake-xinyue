package cn.yuyake.game.message;

import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.IGameMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class FirstMsgResponse implements IGameMessage {

    private Long serverTime; // 返回服务器的时间
    private GameMessageHeader header;
    private byte[] body;

    @Override
    public GameMessageHeader getHeader() {
        return header;
    }

    @Override
    public void setHeader(GameMessageHeader header) {
        this.header = header;
    }

    @Override
    public void read(byte[] body) {
        this.body = body;
    }

    @Override
    public byte[] body() {
        if (body == null) {
            if (serverTime != null) {
                ByteBuf byteBuf = Unpooled.buffer(8);
                byteBuf.writeLong(serverTime);
                body = byteBuf.array();
                if (body == null) {
                    // 检测是否返回的空，防止开发者默认返回null
                    throw new IllegalArgumentException("消息序列化之后的值为null:" + this.getClass().getName());
                }
            }
        }
        return body;
    }

    public Long getServerTime() {
        return serverTime;
    }

    public void setServerTime(Long serverTime) {
        this.serverTime = serverTime;
    }
}
