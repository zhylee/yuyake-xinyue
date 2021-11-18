package cn.yuyake.game.message;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;

@GameMessageMetadata(messageId = 2, messageType = EnumMessageType.REQUEST, serviceId = 1)
public class HeartbeatMsgRequest extends AbstractJsonGameMessage<Void> {


    @Override
    protected Class<Void> getBodyObjClass() {
        return null;
    }
}
