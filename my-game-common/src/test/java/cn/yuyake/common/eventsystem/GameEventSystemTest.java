package cn.yuyake.common.eventsystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {BeanConfig.class}) // 在这里指定需要测试的Bean配置类
public class GameEventSystemTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ApplicationContext context;

    @Test
    public void sendGameEvent() {
        // 创建事件监听实例
        PlayerUpgradeListener playerUpgradeListener = new PlayerUpgradeListener();
        // 注册事件监听类
        GameEventSystem.registerListener(PlayerUpgradeLevelEvent.class, playerUpgradeListener);
        // 模拟产生事件
        PlayerUpgradeLevelEvent event = new PlayerUpgradeLevelEvent();
        event.setPlayerId(1);
        // 发送产生的事件
        GameEventSystem.sendGameEvent(this, event);
    }

    @Test
    public void annotationGameEvent() {
        //先初始化事件系统,在直接项目中是在项目启动的时候调用
        GameEventSystem.start(context);
        PlayerUpgradeLevelEvent event = new PlayerUpgradeLevelEvent();
        event.setPlayerId(1);
        GameEventSystem.sendGameEvent(this, event);
    }
}
