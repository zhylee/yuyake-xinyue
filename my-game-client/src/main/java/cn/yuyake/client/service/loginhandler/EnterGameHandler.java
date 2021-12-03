package cn.yuyake.client.service.loginhandler;

import cn.yuyake.client.service.handler.GameClientChannelContext;
import cn.yuyake.game.message.xinyue.EnterGameMsgResponse;
import cn.yuyake.game.messagedispatcher.GameMessageHandler;
import cn.yuyake.game.messagedispatcher.GameMessageMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GameMessageHandler
public class EnterGameHandler {
    private final Logger logger = LoggerFactory.getLogger(EnterGameHandler.class);

    @GameMessageMapping(EnterGameMsgResponse.class)
    public void enterGameResponse(EnterGameMsgResponse response, GameClientChannelContext ctx) {
        logger.debug("进入游戏成功：{}", response.getBodyObj().getNickname());
    }

}
