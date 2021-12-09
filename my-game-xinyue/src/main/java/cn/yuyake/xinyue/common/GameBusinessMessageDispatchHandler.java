package cn.yuyake.xinyue.common;

import cn.yuyake.dao.AsyncPlayerDao;
import cn.yuyake.db.entity.Player;
import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.game.message.xinyue.GetPlayerByIdMsgResponse;
import cn.yuyake.game.messagedispatcher.DispatchGameMessageService;
import cn.yuyake.gateway.message.channel.AbstractGameChannelHandlerContext;
import cn.yuyake.gateway.message.channel.GameChannelInboundHandler;
import cn.yuyake.gateway.message.channel.GameChannelPromise;
import cn.yuyake.gateway.message.context.DispatchUserEventService;
import cn.yuyake.gateway.message.context.GatewayMessageContext;
import cn.yuyake.gateway.message.context.ServerConfig;
import cn.yuyake.gateway.message.context.UserEventContext;
import cn.yuyake.xinyue.logic.event.GetPlayerInfoEvent;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GameBusinessMessageDispatchHandler implements GameChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(GameBusinessMessageDispatchHandler.class);

    private final DispatchGameMessageService dispatchGameMessageService;
    private final DispatchUserEventService dispatchUserEventService;
    private final ServerConfig serverConfig;
    // private final PlayerDao playerDao;
    private final AsyncPlayerDao playerDao;
    private Player player;
    private ScheduledFuture<?> flushToRedisScheduleFuture;
    private ScheduledFuture<?> flushToDBScheduleFuture;

    public GameBusinessMessageDispatchHandler(
            ServerConfig serverConfig,
            DispatchGameMessageService dispatchGameMessageService,
            DispatchUserEventService dispatchUserEventService,
            AsyncPlayerDao playerDao) {
        this.serverConfig = serverConfig;
        this.dispatchGameMessageService = dispatchGameMessageService;
        this.dispatchUserEventService = dispatchUserEventService;
        this.playerDao = playerDao;
    }

    @Override // 在用户GameChannel注册的时候，对用户的数据进行初始化
    public void channelRegister(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise) {
        playerDao.findPlayer(playerId, new DefaultPromise<>(ctx.executor())).addListener((GenericFutureListener<Future<Optional<Player>>>) future -> {
            Optional<Player> playerOp = future.get();
            if (playerOp.isPresent()) { // 如果查询成功，缓存player信息
                player = playerOp.get();
                promise.setSuccess();
                // 启动定时持久化数据到数据库
                fixTimerFlushPlayer(ctx);
            } else { // 查询失败则返回异常
                logger.error("player {} 不存在", playerId);
                promise.setFailure(new IllegalArgumentException("找不到Player数据，playerId：" + playerId));
            }
        });
    }

    @Override
    public void exceptionCaught(AbstractGameChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("服务器异常，playerId：{}", ctx.gameChannel().getPlayerId(), cause);
    }

    @Override
    public void channelInactive(AbstractGameChannelHandlerContext ctx) throws Exception {
        // 取消DB持久化定时器
        if (flushToDBScheduleFuture != null) {
            // 这里使用参数true，是要打断里面要执行的任务，通过下面的强制方法更新数据
            flushToDBScheduleFuture.cancel(true);
        }
        // 取消Redis持久化定时器
        if (flushToRedisScheduleFuture != null) {
            flushToRedisScheduleFuture.cancel(true);
        }
        // GameChannel移除的时候，强制更新一次数据
        this.playerDao.syncFlushPlayer(player);
        logger.debug("强制flush player {} 成功", player.getPlayerId());
        logger.debug("game channel 移除，playerId：{}", ctx.gameChannel().getPlayerId());
        // 向下一个Handler发送channel失效事件
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(AbstractGameChannelHandlerContext ctx, Object msg) throws Exception {
        IGameMessage gameMessage = (IGameMessage) msg;
        GatewayMessageContext stx = new GatewayMessageContext(player, gameMessage, ctx.gameChannel());
        // 通过反射，调用处理客户端消息的方法
        dispatchGameMessageService.callMethod(gameMessage, stx);
    }

    @Override
    public void userEventTriggered(AbstractGameChannelHandlerContext ctx, Object evt, Promise<Object> promise) throws Exception {
        if (evt instanceof IdleStateEvent) { // 处理GameChannel空闲事件
            UserEventContext utx = new UserEventContext(ctx);
            dispatchUserEventService.callMethod(utx, evt, promise);
        } else if (evt instanceof GetPlayerInfoEvent) { // 处理获取用户信息事件
            GetPlayerByIdMsgResponse response = new GetPlayerByIdMsgResponse();
            response.getBodyObj().setPlayerId(this.player.getPlayerId());
            response.getBodyObj().setNickName(this.player.getNickName());
            Map<String, String> heroes = new HashMap<>();
            // 复制处理一下，防止对象安全溢出
            this.player.getHeroes().forEach(heroes::put);
            // 不要使用这种方式，它会把这个map传递到其他线程
            // response.getBodyObj().setHeroes(this.player.getHeroes());
            response.getBodyObj().setHeroes(heroes);
            promise.setSuccess(response);
        }
    }

    private void fixTimerFlushPlayer(AbstractGameChannelHandlerContext ctx) {
        // 获取定时器执行的延迟时间，单位是秒
        int flushRedisDelay = serverConfig.getFlushRedisDelaySecond();
        int flushDBDelay = serverConfig.getFlushDBDelaySecond();
        // 创建持久化数据到redis的定时任务
        flushToRedisScheduleFuture = ctx.executor().scheduleWithFixedDelay(() -> {
            // 任务开始执行的时间
            long start = System.currentTimeMillis();
            Promise<Boolean> promise = new DefaultPromise<>(ctx.executor());
            playerDao.saveOrUpdatePlayerToRedis(player, promise).addListener((GenericFutureListener<Future<Boolean>>) future -> {
                if (future.isSuccess()) {
                    if (logger.isDebugEnabled()) {
                        long end = System.currentTimeMillis();
                        logger.debug("player {} 同步数据到redis成功，耗时：{} ms", player.getPlayerId(), (end - start));
                    }
                } else {
                    logger.error("player {} 同步数据到Redis失败", player.getPlayerId());
                    // 这个时候应该报警
                }
            });
        }, flushRedisDelay, flushRedisDelay, TimeUnit.SECONDS);
        // 创建持久化数据到db的定时任务
        flushToDBScheduleFuture = ctx.executor().scheduleWithFixedDelay(() -> {
            // 任务开始执行时间
            long start = System.currentTimeMillis();
            Promise<Boolean> promise = new DefaultPromise<>(ctx.executor());
            playerDao.saveOrUpdatePlayerToDB(player, promise).addListener((GenericFutureListener<Future<Boolean>>) future -> {
                if (future.isSuccess()) {
                    if (logger.isDebugEnabled()) {
                        long end = System.currentTimeMillis();
                        logger.debug("player {} 同步数据到MongoDB成功，耗时：{} ms", player.getPlayerId(), (end - start));
                    }
                } else {
                    logger.error("player {} 同步数据到MongoDB失败", player.getPlayerId());
                    // 这个时候应该报警，将数据同步到日志中，以待恢复
                }
            });
        }, flushDBDelay, flushDBDelay, TimeUnit.SECONDS);
    }
}
