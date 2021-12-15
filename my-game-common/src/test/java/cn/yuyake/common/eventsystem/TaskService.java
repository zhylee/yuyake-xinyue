package cn.yuyake.common.eventsystem;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;

@GameEventService
public class TaskService implements ApplicationListener<SpringBootEvent> {

    @GameEventListener(PlayerUpgradeLevelEvent.class)
    public void playerUpgradeEvent(Object origin, PlayerUpgradeLevelEvent event) {
        System.out.println("任务接收到角色升级事件：" + event.getClass().getName());
        // 在这里处理相应的业务逻辑
    }

    @Override // 一个类只能实现一个处理对象
    public void onApplicationEvent(SpringBootEvent event) {
        System.out.println("收到springboot事件:" + event.getClass().getName());
    }

    @EventListener // 基于注解的方式监听处理具体的事件
    public void springBootEvent2(SpringBootEvent2 event) {
        System.out.println("收到springboot2事件：" + event.getClass().getName());
    }
}
