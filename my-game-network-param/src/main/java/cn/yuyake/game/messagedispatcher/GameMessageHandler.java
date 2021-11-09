package cn.yuyake.game.messagedispatcher;

import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 使注解只能标记在类上面
@Retention(RetentionPolicy.RUNTIME) // 在运行时有效
@Service // 让此注解继承 @Service 注解，在项目启动时，自动扫描被 GameMessageHandler 注解的类
public @interface GameMessageHandler {
}
