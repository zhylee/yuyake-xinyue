package cn.yuyake.game.messagedispatcher;

import cn.yuyake.game.common.IGameMessage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 使注解只能标记在方法上面
@Retention(RetentionPolicy.RUNTIME)
public @interface GameMessageMapping {
    // 标记的注解必须赋值消息对象的 Class
    Class<? extends IGameMessage> value();
}
