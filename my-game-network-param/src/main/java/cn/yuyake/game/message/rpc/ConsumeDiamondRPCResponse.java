package cn.yuyake.game.message.rpc;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.rpc.ConsumeDiamondRPCResponse.ResponseBody;

@GameMessageMetadata(messageId = 204, messageType = EnumMessageType.RPC_RESPONSE, serviceId = 102) // 返回的服务id是102服务
public class ConsumeDiamondRPCResponse extends AbstractJsonGameMessage<ResponseBody> {

    public static class ResponseBody {
    }

    @Override
    protected Class<ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }
}
