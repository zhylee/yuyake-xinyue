package cn.yuyake.arena.business;

import cn.yuyake.arena.error.ArenaError;
import cn.yuyake.db.entity.manager.ArenaManager;
import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.game.message.rpc.ConsumeDiamondRPCRequest;
import cn.yuyake.game.message.rpc.ConsumeDiamondRPCResponse;
import cn.yuyake.game.message.xinyue.BuyArenaChallengeTimesMsgRequest;
import cn.yuyake.game.message.xinyue.BuyArenaChallengeTimesMsgResponse;
import cn.yuyake.game.messagedispatcher.GameMessageHandler;
import cn.yuyake.game.messagedispatcher.GameMessageMapping;
import cn.yuyake.gateway.message.context.GatewayMessageContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GameMessageHandler
public class ArenaBusinessHandler {

    private final Logger logger = LoggerFactory.getLogger(ArenaBusinessHandler.class);

    @GameMessageMapping(BuyArenaChallengeTimesMsgRequest.class)
    public void buyChallengeTimes(BuyArenaChallengeTimesMsgRequest request, GatewayMessageContext<ArenaManager> ctx) {
        // 先通过rpc扣除钻石，扣除成功之后，再添加挑战次数
        BuyArenaChallengeTimesMsgResponse response = new BuyArenaChallengeTimesMsgResponse();
        Promise<IGameMessage> rpcPromise = ctx.newRPCPromise();
        // 接收RPC的请求响应结果的回调接口
        rpcPromise.addListener((GenericFutureListener<Future<IGameMessage>>) future -> {
            if (future.isSuccess()) {
                // 接收RPC的返回结果
                ConsumeDiamondRPCResponse rpcResponse = (ConsumeDiamondRPCResponse) future.get();
                int errorCode = rpcResponse.getHeader().getErrorCode();
                // 如果没有错误码，表示扣除成功
                if (errorCode == 0) {
                    // 假设添加10次竞技场挑战次
                    ctx.getDataManager().addChallengeTimes(10);
                    logger.debug("购买竞技挑战次数成功");
                } else {
                    // 否则返回前端错误码
                    response.getHeader().setErrorCode(errorCode);
                }
            } else {
                // 如果出现异常，则返回给客户端一个固定的错误码
                response.getHeader().setErrorCode(ArenaError.SERVER_ERROR.getErrorCode());
            }
            // 向客户端返回消息
            ctx.sendMessage(response);
        });
        // 创建RPC的发送消息
        ConsumeDiamondRPCRequest rpcRequest = new ConsumeDiamondRPCRequest();
        // 假设是20钻石
        rpcRequest.getBodyObj().setConsumeCount(20);
        // 发送RPC消息
        ctx.sendRPCMessage(rpcRequest, rpcPromise);
    }
}
