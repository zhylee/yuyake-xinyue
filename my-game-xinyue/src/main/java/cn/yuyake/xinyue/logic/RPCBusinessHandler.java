package cn.yuyake.xinyue.logic;

import cn.yuyake.db.entity.manager.ArenaManager;
import cn.yuyake.game.message.rpc.ConsumeDiamondRPCRequest;
import cn.yuyake.game.message.rpc.ConsumeDiamondRPCResponse;
import cn.yuyake.game.messagedispatcher.GameMessageHandler;
import cn.yuyake.gateway.message.rpc.RPCEvent;
import cn.yuyake.gateway.message.rpc.RPCEventContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GameMessageHandler
public class RPCBusinessHandler {
    private final Logger logger = LoggerFactory.getLogger(RPCBusinessHandler.class);

    @RPCEvent(ConsumeDiamondRPCRequest.class)
    public void consumeDiamond(RPCEventContext<ArenaManager> ctx, ConsumeDiamondRPCRequest request) {
        logger.debug("收到扣钻石的rpc请求");
        ConsumeDiamondRPCResponse response = new ConsumeDiamondRPCResponse();
        ctx.sendResponse(response);
    }
}
