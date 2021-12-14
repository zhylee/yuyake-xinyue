package cn.yuyake.common.eventsystem;

public interface IGameEventListener {
    // 子类实现这个方法，处理接收到的事件
    void update(Object origin, IGameEventMessage event);
}
