package cn.yuyake.common.eventsystem;

import org.springframework.context.ApplicationContext;

/**
 * 包装整个事件系统的操作，并提供表态方法
 */
public class GameEventSystem {
    // 初始化一个事件分发管理器
    private static final EventDispatchManager eventDispatchManager = new EventDispatchManager();
    private static final GameEventDispatchAnnotationManager gameEventDispatchAnnotationManager = new GameEventDispatchAnnotationManager();

    // 在服务启动的时候，调用此方法，初始化系统中的事件监听
    public static void start(ApplicationContext context) {
        gameEventDispatchAnnotationManager.init(context);
    }

    // 注册监听接口
    public static void registerListener(Class<? extends IGameEventMessage> eventClass, IGameEventListener listener) {
        eventDispatchManager.registerListener(eventClass, listener);
    }

    // 发送事件消息
    public static void sendGameEvent(Object origin, IGameEventMessage gameEventMessage) {
        // 向监听接口中发送事件
        eventDispatchManager.sendGameEvent(origin, gameEventMessage);
        // 向注解监听中发送事件
        gameEventDispatchAnnotationManager.sendGameEvent(gameEventMessage, origin);
    }
}
