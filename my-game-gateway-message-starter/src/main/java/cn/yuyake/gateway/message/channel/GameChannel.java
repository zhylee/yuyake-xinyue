package cn.yuyake.gateway.message.channel;

import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.gateway.message.context.ServerConfig;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 负责管理用户消息事件的接收和发送
 */
public class GameChannel {

    private static final Logger logger = LoggerFactory.getLogger(GameChannel.class);
    // 此Channel所属的线程
    private volatile EventExecutor executor;
    // 发送消息的工厂类接口
    private final IMessageSendFactory messageSendFactory;
    // 处理事件的链表
    private final GameChannelPipeline channelPipeline;
    // 事件分发管理器
    private final GameMessageEventDispatchService gameChannelService;
    // 标记GameChannel是否注册成功
    private volatile boolean registered;
    // 事件等待队列，如果GameChannel还没有注册成功，这个时候又有新的消息过来了，就让事件在这个队列中等待
    private final List<Runnable> waitTaskList = new ArrayList<>(5);
    private final long playerId;
    private int gatewayServerId;
    private final ServerConfig serverConfig;

    public GameChannel(long playerId, GameMessageEventDispatchService gameChannelService, IMessageSendFactory messageSendFactory) {
        this.playerId = playerId;
        this.gameChannelService = gameChannelService;
        this.messageSendFactory = messageSendFactory;
        this.channelPipeline = new GameChannelPipeline(this);
        this.serverConfig = gameChannelService.getApplicationContext().getBean(ServerConfig.class);
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public int getGatewayServerId() {
        return gatewayServerId;
    }

    public long getPlayerId() {
        return playerId;
    }

    public boolean isRegistered() {
        return registered;
    }

    public GameChannelPipeline getChannelPipeline() {
        return channelPipeline;
    }

    public EventExecutor executor() {
        return executor;
    }

    public void register(EventExecutor executor, long playerId) {
        this.executor = executor;
        GameChannelPromise promise = new DefaultGameChannelPromise(this);
        this.channelPipeline.fireRegister(playerId, promise);
        promise.addListener(future -> {
            if (future.isSuccess()) {
                // 注册成功的时候，设置为true
                registered = true;
                // 注册channel成功之后，执行等待的任务，因为此执行这些任务和判断是否注册完成是在同一个线程中，所以此处执行完之后，waitTaskList中不会再有新的任务了
                waitTaskList.forEach(Runnable::run);
            } else {
                gameChannelService.fireInactiveChannel(playerId);
                logger.error("player {} channel 注册失败", playerId, future.cause());
            }
        });
    }

    public void fireChannelInactive() {
        this.safeExecute(this.channelPipeline::fireChannelInactive);
    }

    public void fireReadGameMessage(IGameMessage gameMessage) {
        this.safeExecute(() -> {
            this.gatewayServerId = gameMessage.getHeader().getFromServerId();
            this.channelPipeline.fireChannelRead(gameMessage);
        });
    }

    public void fireUserEvent(Object message, Promise<Object> promise) {
        this.safeExecute(() -> this.channelPipeline.fireUserEventTriggered(message, promise));
    }

    protected void unsafeSendMessage(GameMessagePackage gameMessagePackage, GameChannelPromise promise) {
        this.messageSendFactory.sendMessage(gameMessagePackage, promise);
    }

    public void pushMessage(IGameMessage gameMessage) {
        this.safeExecute(() -> this.channelPipeline.writeAndFlush(gameMessage));
    }

    private void safeExecute(Runnable task) {
        if (this.executor.inEventLoop()) {
            this.safeExecute0(task);
        } else {
            this.executor.execute(() -> {
                this.safeExecute0(task);
            });
        }
    }

    private void safeExecute0(Runnable task) {
        try {
            if (!this.registered) {
                waitTaskList.add(task);
            } else {
                task.run();
            }
        } catch (Throwable e) {
            logger.error("服务器异常", e);
        }
    }

    protected void unsafeClose() {
        this.gameChannelService.fireInactiveChannel(playerId);
    }

    public GameMessageEventDispatchService getEventDispatchService() {
        return this.gameChannelService;
    }
}
