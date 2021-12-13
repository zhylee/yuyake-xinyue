package cn.yuyake.arena.business;

import cn.yuyake.db.entity.manager.ArenaManager;
import cn.yuyake.game.message.xinyue.BuyArenaChallengeTimesMsgRequest;
import cn.yuyake.game.messagedispatcher.GameMessageHandler;
import cn.yuyake.game.messagedispatcher.GameMessageMapping;
import cn.yuyake.gateway.message.context.GatewayMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GameMessageHandler
public class ArenaBusinessHandler {

    private final Logger logger = LoggerFactory.getLogger(ArenaBusinessHandler.class);

    @GameMessageMapping(BuyArenaChallengeTimesMsgRequest.class)
    public void buyChallengeTimes(BuyArenaChallengeTimesMsgRequest request, GatewayMessageContext<ArenaManager> ctx) {
        // TODO 先通过rpc扣除钻石，扣除成功之后，再添加挑战次数
    }
}
