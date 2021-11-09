package cn.yuyake.game;

import cn.yuyake.game.common.AbstractGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.common.IGameMessage;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class GameMessageService {
    private final Logger logger = LoggerFactory.getLogger(GameMessageService.class);
    private final Map<String, Class<? extends IGameMessage>> gameMessageClassMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 初始化的时候，将每个请求的响应的 Message 的 class 和 messageId 对应起来
        Reflections reflections = new Reflections("cn.yuyake");
        // 获取 AbstractGameMessage 所有的子类 Class 对象
        Set<Class<? extends AbstractGameMessage>> classSet = reflections.getSubTypesOf(AbstractGameMessage.class);
        classSet.forEach(c -> {
            GameMessageMetadata messageMetadata = c.getAnnotation(GameMessageMetadata.class);
            if (messageMetadata != null) {
                // 检测元数据是否正常
                this.checkGameMessageMetadata(messageMetadata, c);
                int messageId = messageMetadata.messageId();
                EnumMessageType messageType = messageMetadata.messageType();
                // 根据 messageId 和消息类型枚举获取一个唯一的 key
                String key = this.getMessageClassCacheKey(messageType, messageId);
                // 把 key 和 class 对象建立映射
                gameMessageClassMap.put(key, c);
            }
        });
    }

    private void checkGameMessageMetadata(GameMessageMetadata messageMetadata, Class<? extends AbstractGameMessage> c) {
        int messageId = messageMetadata.messageId();
        if (messageId == 0) {
            this.throwMetadataException("messageId未设置：" + c.getName());
        }
        int serviceId = messageMetadata.serviceId();
        if (serviceId == 0) {
            this.throwMetadataException("serviceId未设置：" + c.getName());
        }
    }

    private void throwMetadataException(String msg) {
        throw new IllegalArgumentException(msg);
    }

    private String getMessageClassCacheKey(EnumMessageType type, int messageId) {
        return messageId + ":" + type.name();
    }

    /**
     * 获取响应数据包的实例
     */
    public IGameMessage getResponseInstanceByMessageId(int messageId) {
        return this.getMessageInstance(EnumMessageType.RESPONSE, messageId);
    }

    /**
     * 获取请求数据包的实例
     */
    public IGameMessage getRequestInstanceByMessageId(int messageId) {
        return this.getMessageInstance(EnumMessageType.REQUEST, messageId);
    }

    // 获取数据反序列化的对象实例
    private IGameMessage getMessageInstance(EnumMessageType messageType, int messageId) {
        String key = this.getMessageClassCacheKey(messageType, messageId);
        Class<? extends IGameMessage> clazz = this.gameMessageClassMap.get(key);
        if (clazz == null) {
            this.throwMetadataException("找不到messageId：" + key + " 对应的响应数据对象 Class");
        }
        IGameMessage gameMessage = null;
        try {
            gameMessage = Objects.requireNonNull(clazz).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            String msg = "实例化响应参数出现，messageId：" + key + "，class：" + clazz.getName();
            logger.error(msg, e);
            this.throwMetadataException(msg);
        } catch (NoSuchMethodException | InvocationTargetException e) {
            String msg = "实例化出现异常，messageId：" + key + "，class：" + clazz.getName();
            logger.error(msg, e);
            this.throwMetadataException(msg);
        }
        return gameMessage;
    }
}
