package cn.yuyake.common.eventsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件分发管理器
 */
public class EventDispatchManager {
    // 缓存监听的事件与事件监听器的映射，由于一个事件对应多个监听器，所以value是一个数组
    private final Map<String, List<IGameEventListener>> eventListenerMap = new HashMap<>();

    // 向事件分发器中注册一个监听类
    public void registerListener(Class<? extends IGameEventMessage> eventClass, IGameEventListener listener) {
        String key = eventClass.getName();
        // 如果事件对应的监听列表不存在，则创建一个新的
        List<IGameEventListener> listeners = this.eventListenerMap.computeIfAbsent(key, i -> new ArrayList<>());
        listeners.add(listener);
    }

    // 发送事件消息
    public void sendGameEvent(Object origin, IGameEventMessage gameEventMessage) {
        String key = gameEventMessage.getClass().getName();
        List<IGameEventListener> listeners = this.eventListenerMap.get(key);
        if (listeners != null) {
            // 获取监听此事件的所有的监听接口列表
            listeners.forEach(listener -> listener.update(origin, gameEventMessage));
        }
    }
}
