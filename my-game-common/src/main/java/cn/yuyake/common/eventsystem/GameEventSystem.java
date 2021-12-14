package cn.yuyake.common.eventsystem;

/**
 * 包装整个事件系统的操作，并提供表态方法
 */
public class GameEventSystem {
    // 初始化一个事件分发管理器
    private static final EventDispatchManager eventDispatchManager = new EventDispatchManager();

    // 注册监听接口
    public static void registerListener(Class<? extends IGameEventMessage> eventClass, IGameEventListener listener) {
        eventDispatchManager.registerListener(eventClass, listener);
    }

    // 发送事件消息
    public static void sendGameEvent(Object origin, IGameEventMessage gameEventMessage) {
        eventDispatchManager.sendGameEvent(origin, gameEventMessage);
    }
}
