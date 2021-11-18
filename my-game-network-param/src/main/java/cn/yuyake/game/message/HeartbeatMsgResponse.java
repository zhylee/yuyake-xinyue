package cn.yuyake.game.message;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.HeartbeatMsgResponse.ResponseBody;

@GameMessageMetadata(messageId = 2, messageType = EnumMessageType.RESPONSE, serviceId = 1)
public class HeartbeatMsgResponse extends AbstractJsonGameMessage<ResponseBody> {

    public static class ResponseBody {
        private long serverTime;

        public long getServerTime() {
            return serverTime;
        }

        public void setServerTime(long serverTime) {
            this.serverTime = serverTime;
        }
    }

    @Override
    protected Class<ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }
}
