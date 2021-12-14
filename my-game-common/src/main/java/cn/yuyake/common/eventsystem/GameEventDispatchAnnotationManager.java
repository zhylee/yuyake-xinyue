package cn.yuyake.common.eventsystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于注解的事件分发管理器
 */
public class GameEventDispatchAnnotationManager {
    private final Logger logger = LoggerFactory.getLogger(GameEventDispatchAnnotationManager.class);
    private final Map<String, List<GameEventListenerMapping>> gameEventMapping = new HashMap<>();

    public void init(ApplicationContext context) {
        // 从上下文中获取标记了@GameEventService注解的所有实例
        context.getBeansWithAnnotation(GameEventService.class).values().forEach(bean -> {
            // 遍历这个bean的所有方法
            Method[] methods = bean.getClass().getMethods();
            for (Method method : methods) {
                GameEventListener gameEventListener = method.getAnnotation(GameEventListener.class);
                // 如果这个方法上面有@GameEventListener注解，说明它需要处理一个事件
                if (gameEventListener != null) {
                    // 记录处理事件的信息
                    Class<? extends IGameEventMessage> eventClass = gameEventListener.value();
                    GameEventListenerMapping gameEventListenerMapping = new GameEventListenerMapping(bean, method);
                    // 这里相当于注册监听接口，把反射调用的信息缓存起来
                    this.addGameEventListenerMapping(eventClass.getName(), gameEventListenerMapping);
                }
            }
        });
    }

    // 将事件的处理信息封装并缓存起来
    private void addGameEventListenerMapping(String key, GameEventListenerMapping gameEventListenerMapping) {
        // 如果缓存中不存在，创建一个新的列表
        List<GameEventListenerMapping> gameEventListenerMappings = this.gameEventMapping.computeIfAbsent(key, k -> new ArrayList<>());
        gameEventListenerMappings.add(gameEventListenerMapping);
    }

    // 发送事件到事件的处理方法中
    public void sendGameEvent(IGameEventMessage gameEventMessage, Object origin) {
        String key = gameEventMessage.getClass().getName();
        List<GameEventListenerMapping> gameEventListenerMappings = this.gameEventMapping.get(key);
        if (gameEventListenerMappings != null) {
            // 找到监听这个事件的所有方法
            gameEventListenerMappings.forEach(c -> {
                try {
                    // 依次调用处理此事件的方法
                    c.getMethod().invoke(c.getBean(), origin, gameEventMessage);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    logger.error("事件发送失败", e);
                    throw new IllegalArgumentException("事件发送失败", e);
                    // 如果捕获到异常，把这个异常抛出去，让上层处理
                }
            });
        }
    }
}
