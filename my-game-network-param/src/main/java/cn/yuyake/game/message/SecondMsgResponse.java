package cn.yuyake.game.message;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.message.SecondMsgResponse.SecondResponseBody;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;

@GameMessageMetadata(messageId = 10002, messageType = EnumMessageType.RESPONSE, serviceId = 1)
public class SecondMsgResponse extends AbstractJsonGameMessage<SecondResponseBody> {

    @Override
    protected Class<SecondResponseBody> getBodyObjClass() {
        return SecondResponseBody.class;
    }

    public static class SecondResponseBody {
        private long result1;
        private String result2;

        public long getResult1() {
            return result1;
        }

        public void setResult1(long result1) {
            this.result1 = result1;
        }

        public String getResult2() {
            return result2;
        }

        public void setResult2(String result2) {
            this.result2 = result2;
        }
    }
}
