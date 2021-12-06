package cn.yuyake.gateway.message.channel;

import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.gateway.message.rpc.GameRpcService;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;

// TODO Game Channel
public class GameChannel {

    public GameChannel(long playerId, GameMessageEventDispatchService gameChannelService, IMessageSendFactory messageSendFactory, GameRpcService gameRpcSendFactory) {

    }

    public void register(EventExecutor executor, long playerId) {

    }

    public void fireReadGameMessage(IGameMessage gameMessage) {

    }

    public void fireUserEvent(Object message, Promise<Object> promise) {

    }

    public void fireChannelInactive() {

    }

    public void pushMessage(IGameMessage gameMessage) {

    }
}
