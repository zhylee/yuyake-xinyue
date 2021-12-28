package cn.yuyake.xinyue.logic;

import cn.yuyake.db.entity.manager.PlayerManager;
import cn.yuyake.game.message.im.SendIMMsgRequest;
import cn.yuyake.game.message.im.SendIMMsgResponse;
import cn.yuyake.game.messagedispatcher.GameMessageHandler;
import cn.yuyake.game.messagedispatcher.GameMessageMapping;
import cn.yuyake.gateway.message.context.GatewayMessageContext;

@GameMessageHandler
public class GameIMHandler {

    @GameMessageMapping(SendIMMsgRequest.class)
    public void sendMsg(SendIMMsgRequest request, GatewayMessageContext<PlayerManager> ctx) {
        String chat = request.getBodyObj().getChat();
        String sender = ctx.getDataManager().getPlayer().getNickName();
        SendIMMsgResponse response = new SendIMMsgResponse();
        response.getBodyObj().setChat(chat);
        response.getBodyObj().setSender(sender);
        ctx.broadcastMessage(response);
    }
}
