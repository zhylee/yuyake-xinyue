package cn.yuyake.game.message.im;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.im.SendIMMsgRequest.SendIMMsgBody;

@GameMessageMetadata(messageId = 311, messageType = EnumMessageType.REQUEST, serviceId = 101)
public class SendIMMsgRequest extends AbstractJsonGameMessage<SendIMMsgBody> {

    public static class SendIMMsgBody {
        private String chat;

        public String getChat() {
            return chat;
        }

        public void setChat(String chat) {
            this.chat = chat;
        }
    }

    @Override
    protected Class<SendIMMsgBody> getBodyObjClass() {
        return SendIMMsgBody.class;
    }
}
