package cn.yuyake.common.eventsystem;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

public class GameEventSystemTest extends AbstractTestNGSpringContextTests {
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
}
