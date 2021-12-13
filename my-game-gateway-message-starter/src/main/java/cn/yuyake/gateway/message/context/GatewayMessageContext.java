package cn.yuyake.gateway.message.context;

import cn.yuyake.db.entity.manager.PlayerManager;
import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.game.messagedispatcher.IGameChannelContext;
import cn.yuyake.gateway.message.channel.GameChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

public class GatewayMessageContext<T> implements IGameChannelContext {

    private final IGameMessage requestMessage;
    private final GameChannel gameChannel;
    private final T dataManager;


    public GatewayMessageContext(T dataManager, IGameMessage requestMessage, GameChannel gameChannel) {
        this.dataManager = dataManager;
        this.requestMessage = requestMessage;
        this.gameChannel = gameChannel;
    }

    public T getDataManager() {
        return dataManager;
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
    public <E> E getRequest() {
        return (E) this.requestMessage;
    }

    @Override
    public String getRemoteHost() {
        return this.requestMessage.getHeader().getAttribute().getClientIp();
    }

    @Override
    public long getPlayerId() {
        return this.requestMessage.getHeader().getPlayerId();
    }

    public Future<Object> sendUserEvent(Object event, Promise<Object> promise, long playerId) {
        this.gameChannel.getEventDispatchService().fireUserEvent(playerId, event, promise);
        return promise;
    }

    public DefaultPromise<Object> newPromise() {
        return new DefaultPromise<>(this.gameChannel.executor());
    }
}
