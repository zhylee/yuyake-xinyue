package cn.yuyake.xinyue.logic;

import cn.yuyake.game.message.xinyue.EnterGameMsgRequest;
import cn.yuyake.game.message.xinyue.EnterGameMsgResponse;
import cn.yuyake.game.messagedispatcher.GameMessageHandler;
import cn.yuyake.game.messagedispatcher.GameMessageMapping;
import cn.yuyake.gateway.message.context.GatewayMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GameMessageHandler
public class PlayerLogicHandler {
    private final Logger logger = LoggerFactory.getLogger(PlayerLogicHandler.class);

    @GameMessageMapping(EnterGameMsgRequest.class) // 标记要处理的请求消息
    public void enterGame(EnterGameMsgRequest request, GatewayMessageContext ctx) {
        logger.info("接收到客户端进入游戏请求：{}", request.getHeader().getPlayerId());
        EnterGameMsgResponse response = new EnterGameMsgResponse();
        response.getBodyObj().setNickname("竹溪");
        response.getBodyObj().setPlayerId(182222788);
        // 给客户端返回消息
        ctx.sendMessage(response);
    }
}
