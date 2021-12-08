package cn.yuyake.gateway.message.context;

import cn.yuyake.game.messagedispatcher.DispatcherMapping;
import cn.yuyake.game.messagedispatcher.GameMessageHandler;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Service
public class DispatchUserEventService {
    private final Logger logger = LoggerFactory.getLogger(DispatchUserEventService.class);
    // 数据缓存
    private final Map<String, DispatcherMapping> userEventMethodCache = new HashMap<>();
    @Autowired // 注入spring上下文类
    private ApplicationContext context;

    @PostConstruct // 项目启动之后，调用此初始化方法
    public void init() {
        // 从spring 容器中获取所有被@GameMessageHandler标记的所有的类实例
        Map<String, Object> beans = context.getBeansWithAnnotation(GameMessageHandler.class);
        // 使用stream并行处理遍历这些对象
        beans.values().parallelStream().forEach(c -> {
            Method[] methods = c.getClass().getMethods();
            // 遍历每个类中的方法
            for (Method method : methods) {
                UserEvent userEvent = method.getAnnotation(UserEvent.class);
                // 如果这个方法被@UserEvent注解标记了，缓存下所有的数据
                if (userEvent != null) {
                    String key = userEvent.value().getName();
                    DispatcherMapping dispatcherMapping = new DispatcherMapping(c, method);
                    userEventMethodCache.put(key, dispatcherMapping);
                }
            }
        });
    }

    // 通过反射调用处理相应事件的方法
    public void callMethod(UserEventContext ctx, Object event, Promise<Object> promise) {
        String key = event.getClass().getName();
        DispatcherMapping dispatcherMapping = this.userEventMethodCache.get(key);
        if (dispatcherMapping != null) {
            try {
                dispatcherMapping.getTargetMethod().invoke(dispatcherMapping.getTargetObj(), ctx, event, promise);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                logger.error("事件处理调用失败，事件对象:{},处理对象：{}，处理方法：{}", event.getClass().getName(), dispatcherMapping.getTargetObj().getClass().getName(), dispatcherMapping.getTargetMethod().getName());
            }
        } else {
            logger.debug("事件：{} 没有找到处理的方法", event.getClass().getName());
        }
    }
}
