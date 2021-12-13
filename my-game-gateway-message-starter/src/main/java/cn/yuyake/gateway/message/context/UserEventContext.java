package cn.yuyake.gateway.message.context;

import cn.yuyake.gateway.message.channel.AbstractGameChannelHandlerContext;

public class UserEventContext<T> {
    private T dataManager;
    private final AbstractGameChannelHandlerContext ctx;

    public UserEventContext(T dataManager, AbstractGameChannelHandlerContext ctx) {
        this.dataManager= dataManager;
        this.ctx = ctx;
    }

    public T getDataManager() {
        return dataManager;
    }

    public AbstractGameChannelHandlerContext getCtx() {
        return ctx;
    }
}
