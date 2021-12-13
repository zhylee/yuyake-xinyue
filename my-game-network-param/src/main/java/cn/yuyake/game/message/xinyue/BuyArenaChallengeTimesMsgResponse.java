package cn.yuyake.game.message.xinyue;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.xinyue.BuyArenaChallengeTimesMsgResponse.ResponseBody;

@GameMessageMetadata(messageId = 210, messageType = EnumMessageType.RESPONSE, serviceId = 102)
public class BuyArenaChallengeTimesMsgResponse extends AbstractJsonGameMessage<ResponseBody> {

    public static class ResponseBody {
        private int times;

        public int getTimes() {
            return times;
        }

        public void setTimes(int times) {
            this.times = times;
        }
    }

    @Override
    protected Class<ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }
}
