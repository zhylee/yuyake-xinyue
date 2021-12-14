package cn.yuyake.common.eventsystem;

// 监听角色升级的事件
public class PlayerUpgradeListener implements IGameEventListener {
    @Override
    public void update(Object origin, IGameEventMessage event) {
        System.out.println("收到事件：" + event.getClass().getName());
        // 在这里处理相关的业务
    }
}
