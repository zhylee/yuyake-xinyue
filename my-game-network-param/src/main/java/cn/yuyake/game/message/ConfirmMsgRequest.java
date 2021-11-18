package cn.yuyake.game.message;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.ConfirmMsgRequest.ConfirmBody;

@GameMessageMetadata(messageId = 1, messageType = EnumMessageType.REQUEST, serviceId = 1)
public class ConfirmMsgRequest extends AbstractJsonGameMessage<ConfirmBody> {

    @Override
    protected Class<ConfirmBody> getBodyObjClass() {
        return ConfirmBody.class;
    }

    public static class ConfirmBody {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
