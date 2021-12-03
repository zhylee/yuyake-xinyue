package cn.yuyake.game.message;

import cn.yuyake.game.common.AbstractGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;

@GameMessageMetadata(messageId = 10001, messageType = EnumMessageType.REQUEST, serviceId = 1) // 添加元数据信息
public class FirstMsgRequest extends AbstractGameMessage {

    private String value;

    @Override
    protected byte[] encode() {
        // 序列化消息，这里不用判断 null，父类上面已判断过
        return value.getBytes();
    }

    @Override
    protected void decode(byte[] body) {
        // 反序列化消息，这里不用判断 null，父类上面已判断过
        value = new String(body);
    }

    @Override
    protected boolean isBodyMsgNull() {
        // 返回要序列化的消息体是否为 null
        return this.value == null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
