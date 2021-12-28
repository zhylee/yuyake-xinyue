package cn.yuyake.game.message.im;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.im.SendIMMsgResponse.IMMsgBody;

@GameMessageMetadata(messageId = 311, messageType = EnumMessageType.RESPONSE, serviceId = 101)
public class SendIMMsgResponse extends AbstractJsonGameMessage<IMMsgBody> {
    public static class IMMsgBody {
        private String chat;
        private String sender;//消息发送者，这里测试，使用昵称，也可以添加一些其它的信息，比如头像，等级等。

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
    protected Class<IMMsgBody> getBodyObjClass() {
        return IMMsgBody.class;
    }

}
