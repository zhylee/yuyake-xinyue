package cn.yuyake.gateway.message.context;

import cn.yuyake.db.entity.Player;
import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.game.messagedispatcher.IGameChannelContext;
import cn.yuyake.gateway.message.channel.GameChannel;

public class GatewayMessageContext implements IGameChannelContext {
    private Player player;
    private IGameMessage requestMessage;
    private GameChannel gameChannel;


    public GatewayMessageContext(Player player, IGameMessage requestMessage, GameChannel gameChannel) {
        this.player = player;
        this.requestMessage = requestMessage;
        this.gameChannel = gameChannel;
    }

    @Override
    public void sendMessage(IGameMessage response) {
        if (response != null) {
            wrapResponseMessage(response);
            gameChannel.getChannelPipeline().writeAndFlush(response);
        }
    }

    private void wrapResponseMessage(IGameMessage response) {
        GameMessageHeader responseHeader = response.getHeader();
        GameMessageHeader requestHeader = this.requestMessage.getHeader();
        responseHeader.setClientSendTime(requestHeader.getClientSendTime());
        responseHeader.setClientSeqId(requestHeader.getClientSeqId());
        responseHeader.setPlayerId(requestHeader.getPlayerId());
        responseHeader.setServerSendTime(System.currentTimeMillis());
        responseHeader.setToServerId(requestHeader.getFromServerId());
        responseHeader.setFromServerId(requestHeader.getToServerId());
        responseHeader.setVersion(requestHeader.getVersion());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getRequest() {
        return (T) this.requestMessage;
    }

    @Override
    public String getRemoteHost() {
        return this.requestMessage.getHeader().getAttribute().getClientIp();
    }

    @Override
    public long getPlayerId() {
        return this.requestMessage.getHeader().getPlayerId();
    }
}
