package cn.yuyake.common.eventsystem;

import java.lang.reflect.Method;

public class GameEventListenerMapping {
    // 处理事件方法所在的bean类
    private final Object bean;
    // 处理事件的方法
    private final Method method;

    public GameEventListenerMapping(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }
}
