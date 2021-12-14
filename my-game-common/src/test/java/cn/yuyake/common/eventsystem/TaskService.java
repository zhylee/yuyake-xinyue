package cn.yuyake.common.eventsystem;

@GameEventService
public class TaskService {

    @GameEventListener(PlayerUpgradeLevelEvent.class)
    public void playerUpgradeEvent(Object origin, PlayerUpgradeLevelEvent event) {
        System.out.println("任务接收到角色升级事件：" + event.getClass().getName());
        // 在这里处理相应的业务逻辑
    }
}
