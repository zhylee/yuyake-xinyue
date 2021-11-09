package cn.yuyake.game.message;

import cn.yuyake.game.common.AbstractGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.body.ThirdMsgBody;
import com.google.protobuf.InvalidProtocolBufferException;

@GameMessageMetadata(messageId = 10003, messageType = EnumMessageType.RESPONSE, serviceId = 1)
public class ThirdMsgResponse extends AbstractGameMessage {
    // 声明消息体
    private ThirdMsgBody.ThirdMsgResponseBody responseBody;

    public ThirdMsgBody.ThirdMsgResponseBody getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(ThirdMsgBody.ThirdMsgResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    protected byte[] encode() {
        // 序列化消息体
        return this.responseBody.toByteArray();
    }

    @Override
    protected void decode(byte[] body) {
        try {
            // 反序列化消息体
            this.responseBody = ThirdMsgBody.ThirdMsgResponseBody.parseFrom(body);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean isBodyMsgNull() {
        // 判断消息体是否为空
        return this.responseBody == null;
    }
}
