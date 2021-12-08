package cn.yuyake.gateway.message.context;

import cn.yuyake.gateway.message.channel.AbstractGameChannelHandlerContext;

public class UserEventContext {
    private final AbstractGameChannelHandlerContext ctx;

    public UserEventContext(AbstractGameChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public AbstractGameChannelHandlerContext getCtx() {
        return ctx;
    }
}
