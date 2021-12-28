package cn.yuyake.game.message.im;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.im.CrossSendIMMsgRequest.SendIMMsgBody;

@GameMessageMetadata(messageId = 312, messageType = EnumMessageType.REQUEST, serviceId = 103)
public class CrossSendIMMsgRequest extends AbstractJsonGameMessage<SendIMMsgBody> {

    public static class SendIMMsgBody {
        private String chat;
        private String sender;

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

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