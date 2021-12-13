package cn.yuyake.dao;

import cn.yuyake.common.concurrent.GameEventExecutorGroup;
import cn.yuyake.db.entity.Arena;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import java.util.Optional;

public class AsyncArenaDao extends AbstractAsyncDao {
    // 注入数据库操作类
    private final ArenaDao arenaDao;

    public AsyncArenaDao(GameEventExecutorGroup executorGroup, ArenaDao arenaDao) {
        // 初始化数据
        super(executorGroup);
        this.arenaDao = arenaDao;
    }

    /**
     * 异步查询数据，这里使用Optional进行封装，由业务判断是否查询结果为空
     */
    public Future<Optional<Arena>> findArena(Long playerId, Promise<Optional<Arena>> promise) {
        this.execute(playerId, promise, () -> {
            Optional<Arena> arena = arenaDao.findById(playerId);
            promise.setSuccess(arena);
        });
        return promise;
    }

    public void updateToRedis(long playerId, Arena arena, Promise<Boolean> promise) {
        this.execute(playerId, promise, () -> {
            arenaDao.saveOrUpdateToRedis(arena, playerId);
            promise.setSuccess(true);
        });
    }

    public void updateToDB(long playerId, Arena arena, Promise<Boolean> promise) {
        this.execute(playerId, promise, () -> {
            arenaDao.saveOrUpdateToDB(arena);
            promise.setSuccess(true);
        });
    }
}
