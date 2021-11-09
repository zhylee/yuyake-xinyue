package cn.yuyake.client.service.handler;

import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.game.messagedispatcher.IGameChannelContext;
import io.netty.channel.Channel;

// TODO Game Client Channel Context
public class GameClientChannelContext implements IGameChannelContext {
    private Channel channel;
    private IGameMessage request;

    public GameClientChannelContext(Channel channel, IGameMessage request) {
        this.channel = channel;
        this.request = request;
    }
}
