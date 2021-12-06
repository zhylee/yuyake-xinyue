package cn.yuyake.xinyue.service;

import cn.yuyake.dao.PlayerDao;
import cn.yuyake.db.entity.Player;
import io.netty.util.concurrent.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class PlayerTest {

    // 声明一个数据库线程池组
    private final EventExecutorGroup dbExecutorGroup = new DefaultEventExecutorGroup(4);
    @Autowired // 注入数据库操作类
    private PlayerDao playerDao;

    /**
     * 1. 利用线程池获取执行结果；这种获取方式会阻塞调用queryPlayer方法的线程
     */
    public Player queryPlayer(long playerId) throws ExecutionException, InterruptedException {
        Future<Player> future = dbExecutorGroup.next().submit(() -> playerDao.findById(playerId).orElse(null));
        // 等待返回查询结果
        return future.get();
    }

    /**
     * 2. 利用异步回调返回结果；但在数据查询完成之后，执行回调不再是调用者线程而是数据库查询的线程
     */
    public void queryPlayer(long playerId, Consumer<Player> consumer) {
        // 异步查询Player，并通过回调方法返回结果
        dbExecutorGroup.next().execute(() -> {
            Player player = playerDao.findById(playerId).orElse(null);
            consumer.accept(player);
        });
    }

    /**
     * 3. Netty线程模型，解决了上面的两个问题
     */
    public Future<Player> queryPlayer(long playerId, Promise<Player> promise) {
        dbExecutorGroup.next().execute(() -> {
            Player player = playerDao.findById(playerId).orElse(null);
            // 查询完之后，设置结果
            promise.setSuccess(player);
        });
        return promise;
    }

    public void test() {
        // asyn test : 2
        this.queryPlayer(1L, player -> { // 当前线程是线程1，调用查询Player的方法
            // 通过回调方式获取Player
            if (player != null) { // 执行回调方法的是数据库查询线程，并不是原来的线程1了
                // 对Player进行其他操作
                player.getMap().forEach((k, v) -> System.out.println(k + "-" + v));
            }
        });
        // future test : 3
        EventExecutor executor = new DefaultEventExecutor();
        executor.execute(() -> {
            Promise<Player> promise = new DefaultPromise<>(executor);
            queryPlayer(1L, promise).addListener((GenericFutureListener<Future<Player>>) future -> {
                // 在listener中处理返回的结果
                if (future.isSuccess()) {
                    // 获取结果
                    Player player = future.get();
                    // 对Player进行其他操作
                    player.getMap().forEach((k, v) -> System.out.println(k + "-" + v));
                }
            });
        });
    }

}
