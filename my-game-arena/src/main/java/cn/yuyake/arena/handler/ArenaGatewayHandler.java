package cn.yuyake.arena.handler;

import cn.yuyake.dao.AsyncArenaDao;
import cn.yuyake.db.entity.Arena;
import cn.yuyake.db.entity.manager.ArenaManager;
import cn.yuyake.gateway.message.channel.AbstractGameChannelHandlerContext;
import cn.yuyake.gateway.message.channel.GameChannelPromise;
import cn.yuyake.gateway.message.handler.AbstractGameMessageDispatchHandler;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

public class ArenaGatewayHandler extends AbstractGameMessageDispatchHandler<ArenaManager> { // 继承抽象类
    // 添加数据管理类
    private ArenaManager arenaManager;
    // 添加数据库异步操作类
    private final AsyncArenaDao asyncArenaDao;

    public ArenaGatewayHandler(ApplicationContext applicationContext) {
        super(applicationContext);
        // 获取操作数据库的类的实例
        this.asyncArenaDao = applicationContext.getBean(AsyncArenaDao.class);
    }

    @Override // 返回数据管理
    protected ArenaManager getDataManager() {
        return arenaManager;
    }

    @Override // GameChannel在第一次创建初始化需要的数据
    protected void initData(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise) {
        // 异步加载竞技场信息
        Promise<Optional<Arena>> arenaPromise = new DefaultPromise<>(ctx.executor());
        asyncArenaDao.findArena(playerId, arenaPromise).addListener((GenericFutureListener<Future<Optional<Arena>>>) future -> {
            if (future.isSuccess()) {
                Optional<Arena> arOptional = future.get();
                if (arOptional.isPresent()) { // 如果存在，放入数据管理类中
                    arenaManager = new ArenaManager(arOptional.get());
                } else { // 如果数据库中不存在，创建一个空对象
                    Arena arena = new Arena();
                    arena.setPlayerId(playerId);
                    arenaManager = new ArenaManager(arena);
                }
                promise.setSuccess();
            } else {
                logger.error("查询竞技场信息失败", future.cause());
                promise.setFailure(future.cause());
            }
        });
    }

    @Override // 更新数据到Redis，用于持久化数据
    protected Future<Boolean> updateToRedis(Promise<Boolean> promise) {
        asyncArenaDao.updateToRedis(playerId, arenaManager.getArena(), promise);
        return promise;
    }

    @Override // 更新数据到MongoDB，用于持久化数据
    protected Future<Boolean> updateToDB(Promise<Boolean> promise) {
        asyncArenaDao.updateToDB(playerId, arenaManager.getArena(), promise);
        return promise;
    }
}
