package cn.yuyake.gateway.message.channel;

import cn.yuyake.common.cloud.GameChannelCloseEvent;
import cn.yuyake.common.concurrent.GameEventExecutorGroup;
import cn.yuyake.game.common.IGameMessage;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * GameChannel的管理类，主要负责管理用户id与GameChannel的映射关系；
 * 一个玩家始终只有一个GameChannel，并负责请求消息的分发。
 */
public class GameMessageEventDispatchService {

    private static final Logger logger = LoggerFactory.getLogger(GameMessageEventDispatchService.class);
    // 管理playerId与GameChannel的集合
    private final Map<Long, GameChannel> gameChannelGroup = new HashMap<>();
    // 业务处理线程池组
    private final GameEventExecutorGroup workerGroup;
    // 当前管理gameChannelGroup集合的事件线程池
    private final EventExecutor executor;
    // 向客户端发送消息的接口类。可以根据需求，有不同的实现，这里默认是发送到kafka的消息总线服务中
    private final IMessageSendFactory messageSendFactory;

    private final GameChannelInitializer channelInitializer;

    private final ApplicationContext context;

    public GameMessageEventDispatchService(
            ApplicationContext context,
            GameEventExecutorGroup workerGroup,
            IMessageSendFactory messageSendFactory,
            GameChannelInitializer channelInitializer) {
        this.executor = workerGroup.next();
        this.workerGroup = workerGroup;
        this.messageSendFactory = messageSendFactory;
        this.channelInitializer = channelInitializer;
        this.context = context;
    }

    public ApplicationContext getApplicationContext() {
        return context;
    }

    // 此方法保证所有操作gameChannelGroup集合的行为都在同一个线程中执行，避免跨线程操作
    private void safeExecute(Runnable task) { // 将方法的请求变成事件，在此类所属的事件线程池中执行
        if (this.executor.inEventLoop()) {
            // 如果当前调用这个方法的线程和此类所属的线程是同一个线程，则可以立刻执行执行
            try {
                task.run();
            } catch (Throwable e) {
                logger.error("服务器内部错误", e);
            }
        } else {
            // 如果当前调用这个方法的线程和此类所属的线程不是同一个线程，将此任务提交到线程池中等待执行
            this.executor.execute(() -> {
                try {
                    task.run();
                } catch (Throwable e) {
                    logger.error("服务器内部错误", e);
                }
            });
        }
    }

    private GameChannel getGameChannel(Long playerId) {
        return this.gameChannelGroup.computeIfAbsent(playerId, i -> {
            // 从集合中获取一个GameChannel，如果这个GameChannel为空，则重新创建，并初始化注册这个Channel
            var newChannel = new GameChannel(playerId, this, messageSendFactory);
            // 初始化Channel，可以通过这个接口向GameChannel中添加处理消息的Handler
            this.channelInitializer.initChannel(newChannel);
            // 发注册GameChannel的事件
            newChannel.register(workerGroup.select(playerId), playerId);
            return newChannel;
        });
    }

    // 发送接收到的消息事件
    public void fireReadMessage(Long playerId, IGameMessage message) {
        this.safeExecute(() -> {
            GameChannel gameChannel = this.getGameChannel(playerId);
            gameChannel.fireReadGameMessage(message);
        });
    }

    // 发送用户定义的事件
    public void fireUserEvent(Long playerId, Object msg, Promise<Object> promise) {
        this.safeExecute(() -> {
            GameChannel gameChannel = this.getGameChannel(playerId);
            gameChannel.fireUserEvent(msg, promise);
        });
    }

    // 发送GameChannel失效的事件，在这个事件中可以处理一些数据落地的操作
    public void fireInactiveChannel(Long playerId) {
        this.safeExecute(() -> {
            GameChannel gameChannel = this.gameChannelGroup.remove(playerId);
            if (gameChannel != null) {
                gameChannel.fireChannelInactive();
                // 发布GameChannel失效事件
                GameChannelCloseEvent event = new GameChannelCloseEvent(this, playerId);
                context.publishEvent(event);
            }
        });
    }

    // 发送消息广播事件，客多个客户端发送消息
    public void broadcastMessage(IGameMessage gameMessage, long... playerIds) {
        if (playerIds == null || playerIds.length == 0) {
            logger.debug("广播的对象集合为空，直接返回");
            return;
        }
        this.safeExecute(() -> {
            for (long playerId : playerIds) {
                if (this.gameChannelGroup.containsKey(playerId)) {
                    GameChannel gameChannel = this.getGameChannel(playerId);
                    gameChannel.pushMessage(gameMessage);
                }
            }
        });
    }
}
