package cn.yuyake.gateway.message.context;

import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.game.messagedispatcher.IGameChannelContext;
import cn.yuyake.gateway.message.channel.AbstractGameChannelHandlerContext;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

public class GatewayMessageContext<T> implements IGameChannelContext {

    private final IGameMessage requestMessage;
    private final AbstractGameChannelHandlerContext ctx;
    private final T dataManager;


    public GatewayMessageContext(T dataManager, IGameMessage requestMessage, AbstractGameChannelHandlerContext ctx) {
        this.dataManager = dataManager;
        this.requestMessage = requestMessage;
        this.ctx = ctx;
    }

    public T getDataManager() {
        return dataManager;
    }

    @Override
    public void sendMessage(IGameMessage response) {
        if (response != null) {
            wrapResponseMessage(response);
            this.ctx.writeAndFlush(response);
        }
    }

    public Future<IGameMessage> sendRPCMessage(IGameMessage rpcRequest, Promise<IGameMessage> callback) {
        if (rpcRequest != null) {
            rpcRequest.getHeader().setPlayerId(ctx.gameChannel().getPlayerId());
            this.ctx.writeRPCMessage(rpcRequest, callback);
        } else {
            throw new NullPointerException("RPC消息不能为空");
        }
        return callback;
    }

    public void sendRPCMessage(IGameMessage rpcRequest) {
        if (rpcRequest != null) {
            ctx.writeRPCMessage(rpcRequest, null);
        } else {
            throw new NullPointerException("RPC消息不能为空");
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
        this.ctx.gameChannel().getEventDispatchService().fireUserEvent(playerId, event, promise);
        return promise;
    }

    public <E> DefaultPromise<E> newPromise() {
        return new DefaultPromise<>(this.ctx.executor());
    }

    public DefaultPromise<IGameMessage> newRPCPromise() {
        return new DefaultPromise<>(this.ctx.executor());
    }
}
