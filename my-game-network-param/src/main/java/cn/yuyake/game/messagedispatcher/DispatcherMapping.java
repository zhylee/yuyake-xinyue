package cn.yuyake.game.messagedispatcher;

import java.lang.reflect.Method;

public class DispatcherMapping {

    private final Object targetObj; // 处理消息的目标对象
    private final Method targetMethod; // 处理消息的目标方法

    public DispatcherMapping(Object targetObj, Method targetMethod) {
        this.targetObj = targetObj;
        this.targetMethod = targetMethod;
    }

    public Object getTargetObj() {
        return targetObj;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }
}
