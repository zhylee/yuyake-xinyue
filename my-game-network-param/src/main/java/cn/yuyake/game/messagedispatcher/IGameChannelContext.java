package cn.yuyake.game.messagedispatcher;

import cn.yuyake.game.common.IGameMessage;

public interface IGameChannelContext {

    void sendMessage(IGameMessage gameMessage);

    <T> T getRequest();

    String getRemoteHost();

    long getPlayerId();
}
