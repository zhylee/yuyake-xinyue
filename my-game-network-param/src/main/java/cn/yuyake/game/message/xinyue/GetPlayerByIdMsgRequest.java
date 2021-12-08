package cn.yuyake.game.message.xinyue;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.xinyue.GetPlayerByIdMsgRequest.RequestBody;

@GameMessageMetadata(messageId = 202, messageType = EnumMessageType.REQUEST, serviceId = 101)
public class GetPlayerByIdMsgRequest extends AbstractJsonGameMessage<RequestBody> {

    public static class RequestBody {
        private int playerId;

        public int getPlayerId() {
            return playerId;
        }

        public void setPlayerId(int playerId) {
            this.playerId = playerId;
        }
    }

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }
}
