package cn.yuyake.gateway.message.rpc;

import cn.yuyake.game.common.IGameMessage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RPCEvent {
    Class<? extends IGameMessage> value();
}
