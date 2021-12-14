package cn.yuyake.game.message.rpc;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.rpc.ConsumeDiamondRPCRequest.RequestBody;

@GameMessageMetadata(messageId = 204, messageType = EnumMessageType.RPC_REQUEST, serviceId = 101)
public class ConsumeDiamondRPCRequest extends AbstractJsonGameMessage<RequestBody> {

    public static class RequestBody {
        private int consumeCount;

        public int getConsumeCount() {
            return consumeCount;
        }

        public void setConsumeCount(int consumeCount) {
            this.consumeCount = consumeCount;
        }
    }

    @Override
    protected Class<RequestBody> getBodyObjClass() {
        return RequestBody.class;
    }
}
