package cn.yuyake.game.message;

import cn.yuyake.game.common.AbstractGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.body.ThirdMsgBody;
import com.google.protobuf.InvalidProtocolBufferException;

@GameMessageMetadata(messageId = 10003, messageType = EnumMessageType.REQUEST, serviceId = 1)
public class ThirdMsgRequest extends AbstractGameMessage {

    // 消息体使用 Protocol Buffers 生成的类
    private ThirdMsgBody.ThirdMsgRequestBody requestBody;

    public ThirdMsgBody.ThirdMsgRequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(ThirdMsgBody.ThirdMsgRequestBody requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    protected byte[] encode() {
        // 使用 Protocol Buffers 的方式将消息体序列化
        return this.requestBody.toByteArray();
    }

    @Override
    protected void decode(byte[] body) {
        try {
            // 使用 Protocol Buffers 的方式反序列化消息体
            this.requestBody = ThirdMsgBody.ThirdMsgRequestBody.parseFrom(body);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean isBodyMsgNull() {
        // 判断消息体是否为空
        return this.requestBody == null;
    }
}
