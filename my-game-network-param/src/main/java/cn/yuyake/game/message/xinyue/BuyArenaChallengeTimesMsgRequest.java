package cn.yuyake.game.message.xinyue;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.xinyue.BuyArenaChallengeTimesMsgRequest.RequestBody;

@GameMessageMetadata(messageId = 210, messageType = EnumMessageType.REQUEST, serviceId = 102)
public class BuyArenaChallengeTimesMsgRequest extends AbstractJsonGameMessage<RequestBody> {

    public static class RequestBody {
    }

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }
}
