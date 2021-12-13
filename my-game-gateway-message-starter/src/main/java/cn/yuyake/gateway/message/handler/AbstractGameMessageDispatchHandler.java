package cn.yuyake.gateway.message.handler;

import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.game.messagedispatcher.DispatchGameMessageService;
import cn.yuyake.gateway.message.channel.AbstractGameChannelHandlerContext;
import cn.yuyake.gateway.message.channel.GameChannelInboundHandler;
import cn.yuyake.gateway.message.channel.GameChannelPromise;
import cn.yuyake.gateway.message.context.DispatchUserEventService;
import cn.yuyake.gateway.message.context.GatewayMessageContext;
import cn.yuyake.gateway.message.context.ServerConfig;
import cn.yuyake.gateway.message.context.UserEventContext;
import io.netty.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

public abstract class AbstractGameMessageDispatchHandler<T> implements GameChannelInboundHandler {

    private final DispatchGameMessageService dispatchGameMessageService;
    private final DispatchUserEventService dispatchUserEventService;
    private final ServerConfig serverConfig;
    private ScheduledFuture<?> flushToRedisScheduleFuture;
    private ScheduledFuture<?> flushToDBScheduleFuture;

    protected long playerId;
    protected Logger logger;
    protected int gatewayServerId;

    public AbstractGameMessageDispatchHandler(ApplicationContext applicationContext) {
        this.dispatchGameMessageService = applicationContext.getBean(DispatchGameMessageService.class);
        this.dispatchUserEventService = applicationContext.getBean(DispatchUserEventService.class);
        this.serverConfig = applicationContext.getBean(ServerConfig.class);
        logger = LoggerFactory.getLogger(this.getClass());
    }

    // 返回数据管理
    protected abstract T getDataManager();

    // GameChannel在第一次创建初始化需要的数据
    protected abstract void initData(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise);

    // 更新数据到Redis，用于持久化数据
    protected abstract Future<Boolean> updateToRedis(Promise<Boolean> promise);

    // 更新数据到MongoDB，用于持久化数据
    protected abstract Future<Boolean> updateToDB(Promise<Boolean> promise);


    @Override
    public void exceptionCaught(AbstractGameChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void channelRegister(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise) {
        this.playerId = playerId;
        GameChannelPromise initPromise = ctx.newPromise();
        initPromise.addListener(future -> {
            // 初始化成功之后，启动定时器，定时持久化数据
            fixTimerFlushPlayer(ctx);
            promise.setSuccess();
        });
        this.initData(ctx, playerId, initPromise);
    }

    @Override
    public void channelInactive(AbstractGameChannelHandlerContext ctx) throws Exception {
        // 取消定时器
        if (flushToDBScheduleFuture != null) {
            flushToDBScheduleFuture.cancel(true);
        }
        if (flushToRedisScheduleFuture != null) {
            flushToRedisScheduleFuture.cancel(true);
        }
        this.updateToRedis0(ctx);
        this.updateToDB0(ctx);
        logger.debug("game channel 移除，playerId：{}", ctx.gameChannel().getPlayerId());
        // 向下一个Handler发送channel失效事件
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(AbstractGameChannelHandlerContext ctx, Object msg) throws Exception {
        IGameMessage gameMessage = (IGameMessage) msg;
        T dataManager = this.getDataManager();
        GatewayMessageContext<T> stx = new GatewayMessageContext<>(dataManager, gameMessage, ctx.gameChannel());
        dispatchGameMessageService.callMethod(gameMessage, stx);
    }

    @Override
    public void userEventTriggered(AbstractGameChannelHandlerContext ctx, Object evt, Promise<Object> promise) throws Exception {
        T data = this.getDataManager();
        UserEventContext<T> utx = new UserEventContext<>(data, ctx);
        dispatchUserEventService.callMethod(utx, evt, promise);
    }

    private void fixTimerFlushPlayer(AbstractGameChannelHandlerContext ctx) {
        // 获取定时器执行的延迟时间，单位是秒
        int flushRedisDelay = serverConfig.getFlushRedisDelaySecond();
        int flushDBDelay = serverConfig.getFlushDBDelaySecond();
        // 创建持久化数据到redis的定时任务
        flushToRedisScheduleFuture = ctx.executor().scheduleWithFixedDelay(() -> this.updateToRedis0(ctx), flushRedisDelay, flushRedisDelay, TimeUnit.SECONDS);
        flushToDBScheduleFuture = ctx.executor().scheduleWithFixedDelay(() -> this.updateToDB0(ctx), flushDBDelay, flushDBDelay, TimeUnit.SECONDS);
    }

    private void updateToRedis0(AbstractGameChannelHandlerContext ctx) {
        // 任务开始执行的时间
        long start = System.currentTimeMillis();
        Promise<Boolean> promise = new DefaultPromise<>(ctx.executor());
        this.updateToRedis(promise).addListener((GenericFutureListener<Future<Boolean>>) future -> {
            if (future.isSuccess()) {
                if (logger.isDebugEnabled()) {
                    long end = System.currentTimeMillis();
                    logger.debug("player {} 同步数据到redis成功，耗时：{} ms", playerId, (end - start));
                }
            } else {
                logger.error("player {} 同步数据到Redis失败", playerId);
                // 这个时候应该报警
            }
        });
    }

    private void updateToDB0(AbstractGameChannelHandlerContext ctx) {
        // 任务开始执行时间
        long start = System.currentTimeMillis();
        Promise<Boolean> promise = new DefaultPromise<>(ctx.executor());
        updateToDB(promise).addListener((GenericFutureListener<Future<Boolean>>) future -> {
            if (future.isSuccess()) {
                if (logger.isDebugEnabled()) {
                    long end = System.currentTimeMillis();
                    logger.debug("player {} 同步数据到MongoDB成功，耗时：{} ms", playerId, (end - start));
                }
            } else {
                logger.error("player {} 同步数据到MongoDB失败", playerId);
                // 这个时候应该报警,将数据同步到日志中，以待恢复
            }
        });
    }
}
