package cn.yuyake.gateway.message.channel;

import cn.yuyake.game.common.GameMessagePackage;

public interface IMessageSendFactory {

    void sendMessage(GameMessagePackage gameMessagePackage, GameChannelPromise promise);
}
