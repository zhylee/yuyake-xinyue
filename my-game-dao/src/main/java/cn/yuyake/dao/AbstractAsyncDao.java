package cn.yuyake.dao;

import cn.yuyake.common.concurrent.GameEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractAsyncDao {
    // 日志在子类型创建时创建
    protected final Logger logger;
    // 异步处理需要的线程组
    private final GameEventExecutorGroup executorGroup;

    // 初始化
    public AbstractAsyncDao(GameEventExecutorGroup executorGroup) {
        this.executorGroup = executorGroup;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    protected void execute(long playerId, Promise<?> promise, Runnable task) {
        EventExecutor executor = this.executorGroup.select(playerId);
        executor.execute(() -> {
            try {
                task.run(); // 执行任务
            } catch (Throwable e) { // 统一进行异常捕获，防止由于数据库查询的异常导到线程卡死
                logger.error("数据库操作失败，playerId：{}", playerId, e);
                if (promise != null) {
                    promise.setFailure(e);
                }
            }
        });
    }
}
