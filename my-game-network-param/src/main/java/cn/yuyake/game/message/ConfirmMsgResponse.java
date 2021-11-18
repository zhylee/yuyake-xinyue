package cn.yuyake.game.message;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.ConfirmMsgResponse.ConfirmResponseBody;

@GameMessageMetadata(messageId = 1, messageType = EnumMessageType.RESPONSE, serviceId = 1)
public class ConfirmMsgResponse extends AbstractJsonGameMessage<ConfirmResponseBody> {

    @Override
    protected Class<ConfirmResponseBody> getBodyObjClass() {
        return ConfirmResponseBody.class;
    }

    public static class ConfirmResponseBody {
        // 对称加密密钥，客户端需要使用非对称加密私钥解密才能获得
        private String secretKey;

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

    }
}
