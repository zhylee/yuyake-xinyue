package cn.yuyake.game.messagedispatcher;

import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.common.IGameMessage;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class DispatchGameMessageService {
    private final Logger logger = LoggerFactory.getLogger(DispatchGameMessageService.class);
    private final Map<String, DispatcherMapping> dispatcherMappingMap = new HashMap<>();
    @Autowired // 注入 spring 上下文
    private ApplicationContext applicationContext;

    /**
     * 服务启动的时候调用此方法，扫描获取此服务要处理的 game message 类
     *
     * @param applicationContext 上下文
     * @param serviceId          服务id。如果为0，则加载所有的消息类型；如果不为零，则只加载此类型的消息
     * @param packagePath        消息所在的包路径
     */
    public static void scanGameMessages(ApplicationContext applicationContext, int serviceId, String packagePath) {
        // 构建一个方便的调用方法
        DispatchGameMessageService dispatchGameMessageService =
                applicationContext.getBean(DispatchGameMessageService.class);
        dispatchGameMessageService.scanGameMessages(serviceId, packagePath);
    }

    private void scanGameMessages(int serviceId, String packagePath) {
        Reflections reflections = new Reflections(packagePath);
        // 扫描指定的包路径下的所有类，根据注解，获取所有标记了这个注解的所有类的 Class
        Set<Class<?>> allGameMessageHandlerClass = reflections.getTypesAnnotatedWith(GameMessageHandler.class);
        if (allGameMessageHandlerClass == null) {
            return;
        }
        // 遍历获得的所有的 Class
        allGameMessageHandlerClass.forEach(c -> {
            // 根据 Class 从 spring 中获取它的实例，从 spring 中获取实例的好处是，把处理消息的类纳入到 spring 的管理体系中
            Object targetObject = applicationContext.getBean(c);
            Method[] methods = c.getMethods();
            // 遍历这个类上面的所有方法
            for (Method m : methods) {
                GameMessageMapping gameMessageMapping = m.getAnnotation(GameMessageMapping.class);
                // 判断此方法上面是否有 GameMessageMapping
                if (gameMessageMapping != null) {
                    // 从注解中获取处理的 IGameMessage 对象的 Class
                    Class<?> gameMessageClass = gameMessageMapping.value();
                    GameMessageMetadata gameMessageMetadata = gameMessageClass.getAnnotation(GameMessageMetadata.class);
                    // 每个服务只加载自己可以处理的消息类型，如果为 0 则加载所有的类型
                    if (serviceId == 0 || gameMessageMetadata.serviceId() == serviceId) {
                        DispatcherMapping dispatcherMapping = new DispatcherMapping(targetObject, m);
                        this.dispatcherMappingMap.put(gameMessageClass.getName(), dispatcherMapping);
                    }
                }
            }
        });
    }

    /**
     * 当收到网络消息之后，调用此方法
     */
    public void callMethod(IGameMessage gameMessage, IGameChannelContext ctx) {
        String key = gameMessage.getClass().getName();
        // 根据消息的 ClassName 找到调用方法的信息
        DispatcherMapping dispatcherMapping = this.dispatcherMappingMap.get(key);
        if (dispatcherMapping != null) {
            Object obj = dispatcherMapping.getTargetObj();
            try {
                // 调用处理消息的方法
                dispatcherMapping.getTargetMethod().invoke(obj, gameMessage, ctx);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                logger.error("调用方法异常，方法所在类：{}，方法名：{}", obj.getClass().getName(),
                        dispatcherMapping.getTargetMethod().getName(), e);
            }
        } else {
            logger.warn("消息未找到处理的方法，消息名：{}", key);
        }
    }
}
