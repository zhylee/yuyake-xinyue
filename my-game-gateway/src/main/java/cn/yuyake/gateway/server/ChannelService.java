package cn.yuyake.gateway.server;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;

/**
 * 连接管理
 */
@Service
public class ChannelService {
    // playerId 与 Netty Channel 的映射容器
    // 这里使用的是 HashMap，所以对于 Map 的操作都要放在锁里面
    private final Map<Long, Channel> playerChannelMap = new HashMap<>();
    // 读写锁，使用非公平锁
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    // 日志
    private final Logger logger = LoggerFactory.getLogger(ChannelService.class);

    // 封装读锁，统一添加，防止写错
    private void readLock(Runnable task) {
        // 加锁
        lock.readLock().lock();
        try {
            // 统一异常捕获
            task.run();
        } catch (Exception e) {
            logger.error("ChannelService 读锁处理异常", e);
        } finally {
            // 解锁
            lock.readLock().unlock();
        }
    }

    // 封装写锁，统一添加，防止写错
    private void writeLock(Runnable task) {
        lock.writeLock().lock();
        try {
            task.run();
        } catch (Exception e) {
            logger.error("ChannelService 写锁处理异常", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 添加 Channel
     */
    public void addChannel(Long playerId, Channel channel) {
        // 数据写入，添加写锁
        this.writeLock(() -> playerChannelMap.put(playerId, channel));
    }

    /**
     * 获取 Channel
     */
    public Channel getChannel(Long playerId) {
        lock.readLock().lock();
        try {
            Channel channel = this.playerChannelMap.get(playerId);
            return channel;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 移除 Channel
     */
    public void removeChannel(Long playerId, Channel removedChannel) {
        this.writeLock(() -> {
            Channel existChannel = playerChannelMap.get(playerId);
            if (existChannel != null && existChannel == removedChannel) {
                // 必须是同一个对象才可以移除
                playerChannelMap.remove(playerId);
                existChannel.close();
            }
        });
    }

    /**
     * 向 Channel 广播消息
     */
    public void broadcast(BiConsumer<Long, Channel> consumer) {
        this.readLock(() -> this.playerChannelMap.forEach(consumer));
    }

    /**
     * 获取当前连接的数量
     */
    public int getChannelCount() {
        lock.writeLock().lock();
        try {
            int size = this.playerChannelMap.size();
            return size;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
