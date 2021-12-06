package cn.yuyake.common.concurrent;

import io.netty.util.concurrent.*;
import io.netty.util.internal.SystemPropertyUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 相同的playerId，返回相同的EventExecutor实例
 */
public class GameEventExecutorGroup extends AbstractEventExecutorGroup {
    // 线程组中线程数量
    private final EventExecutor[] children;
    private final AtomicInteger childIndex = new AtomicInteger();
    private final AtomicInteger terminatedChildren = new AtomicInteger();
    @SuppressWarnings("rawtypes")
    private final Promise<?> terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
    // 单个线程中任务的排队最大数量
    static final int DEFAULT_MAX_PENDING_EXECUTOR_TASKS = Math.max(16,
            SystemPropertyUtil.getInt("io.netty.eventexecutor.maxPendingTasks", Integer.MAX_VALUE));

    public GameEventExecutorGroup(int nThreads) {
        this(nThreads, null);
    }

    public GameEventExecutorGroup(int nThreads, ThreadFactory threadFactory) {
        this(nThreads, threadFactory, DEFAULT_MAX_PENDING_EXECUTOR_TASKS, RejectedExecutionHandlers.reject());
    }

    public GameEventExecutorGroup(int nThreads, ThreadFactory threadFactory, int maxPendingTasks, RejectedExecutionHandler rejectedHandler) {
        if (nThreads <= 0) {
            throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", nThreads));
        }
        if (threadFactory == null) {
            // 使用默认的线程创建工厂
            threadFactory = new DefaultThreadFactory(getClass());
        }
        // 创建线程组
        children = new SingleThreadEventExecutor[nThreads];
        for (int i = 0; i < nThreads; i++) {
            boolean success = false;
            try {
                // 创建具体的EventExecutor
                children[i] = new DefaultEventExecutor(this, threadFactory, maxPendingTasks, rejectedHandler);
                success = true;
            } catch (Exception e) {
                throw new IllegalStateException("failed to create a child event loop", e);
            } finally {
                if (!success) { // 如果没有成功，需要关闭已创建的EventExecutor
                    for (int j = 0; j < i; j++) {
                        children[j].shutdownGracefully();
                    }
                    for (int j = 0; j < i; j++) {
                        EventExecutor e = children[j];
                        try {
                            while (!e.isTerminated()) {
                                e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                            }
                        } catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }
        // 创建停止监听接口
        final FutureListener<Object> terminationListener = future -> {
            if (terminatedChildren.incrementAndGet() == children.length) {
                terminationFuture.setSuccess(null);
            }
        };
        for (EventExecutor e : children) {
            e.terminationFuture().addListener(terminationListener);
        }
    }

    @Override // 按顺序获取一个EventExecutor
    public EventExecutor next() {
        return this.getEventExecutor(childIndex.getAndIncrement());
    }

    // 根据某一个key获取EventExecutor，如果key相同，证明获取的是同一个EventExecutor
    public EventExecutor select(Object selectKey) {
        if (selectKey == null) {
            throw new IllegalArgumentException("selectKey不能为空");
        }
        int hashCode = selectKey.hashCode();
        return this.getEventExecutor(hashCode);
    }

    // 根据索引值选择一个EventExecutor
    private EventExecutor getEventExecutor(int value) {
        if (isPowerOfTwo(this.children.length)) {
            return children[value & children.length - 1];
        } else {
            return children[Math.abs(value % children.length)];
        }
    }

    private static boolean isPowerOfTwo(int val) {
        return (val & -val) == val;
    }

    @Override
    public boolean isShuttingDown() {
        for (EventExecutor l : children) {
            if (!l.isShuttingDown()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        for (EventExecutor l : children) {
            l.shutdownGracefully(quietPeriod, timeout, unit);
        }
        return terminationFuture();
    }

    @Override
    public Future<?> terminationFuture() {
        return terminationFuture;
    }

    @Override
    public void shutdown() {
        this.shutdownGracefully();
    }

    @Override
    public boolean isShutdown() {
        for (EventExecutor l : children) {
            if (!l.isShutdown()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isTerminated() {
        for (EventExecutor l : children) {
            if (!l.isTerminated()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        loop: for (EventExecutor l : children) {
            for (;;) {
                long timeLeft = deadline - System.nanoTime();
                if (timeLeft <= 0) {
                    break loop;
                }
                if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS)) {
                    break;
                }
            }
        }
        return isTerminated();
    }

    @Override
    public Iterator<EventExecutor> iterator() {
        Set<EventExecutor> children = Collections.newSetFromMap(new LinkedHashMap<>());
        Collections.addAll(children, this.children);
        return children.iterator();
    }
}
