package cn.yuyake.common.eventsystem;

import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Service // 标记了整个注解的类可以作为Bean被Spring容器管理
public @interface GameEventService {
}
