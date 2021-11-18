package cn.yuyake.client.service.handler;

import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.game.messagedispatcher.IGameChannelContext;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class GameClientChannelContext implements IGameChannelContext {
    private final Channel channel;
    private final IGameMessage request;
    private final static Logger logger = LoggerFactory.getLogger(GameClientChannelContext.class);

    public GameClientChannelContext(Channel channel, IGameMessage request) {
        this.channel = channel;
        this.request = request;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public void sendMessage(IGameMessage gameMessage) {
        if (channel.isActive() && channel.isOpen()) {
            channel.writeAndFlush(gameMessage);
        } else {
            logger.trace("channel {} 已失效，发消息失败", channel.id().asShortText());
        }
    }

    @Override
    public String getRemoteHost() {
        InetSocketAddress ipSocket = (InetSocketAddress) channel.remoteAddress();
        String remoteHost = ipSocket.getAddress().getHostAddress();
        return remoteHost;
    }

    @Override
    public long getPlayerId() {
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getRequest() {
        return (T) request;
    }
}
