package cn.yuyake.game.message;

import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.IGameMessage;

public class FirstMsgRequest implements IGameMessage {
    private String value;
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
        if (body != null) {
            // 如果不为null，才反序列化，这样不用考虑为null的情况，防止忘记判断。
            value = new String(body);
        }
    }

    @Override
    public byte[] body() {
        return body;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
