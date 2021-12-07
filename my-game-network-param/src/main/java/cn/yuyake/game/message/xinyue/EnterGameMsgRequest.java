package cn.yuyake.game.message.xinyue;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.xinyue.EnterGameMsgRequest.RequestBody;

@GameMessageMetadata(messageId = 201, messageType = EnumMessageType.REQUEST, serviceId = 101)
public class EnterGameMsgRequest extends AbstractJsonGameMessage<RequestBody> {

    public static class RequestBody {

    }

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }
}
