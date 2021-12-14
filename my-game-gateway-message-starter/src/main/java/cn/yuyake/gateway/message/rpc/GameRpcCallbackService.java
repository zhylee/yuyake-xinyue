package cn.yuyake.gateway.message.rpc;

import cn.yuyake.common.error.GameErrorException;
import cn.yuyake.game.common.IGameMessage;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GameRpcCallbackService {
    private final Map<Integer, Promise<IGameMessage>> callbackMap = new ConcurrentHashMap<>();
    private final EventExecutorGroup eventExecutorGroup;
    private static final int timeout = 30;// 超时时间，30s

    public GameRpcCallbackService(EventExecutorGroup eventExecutorGroup) {
        this.eventExecutorGroup = eventExecutorGroup;
    }

    public void addCallback(int seqId, Promise<IGameMessage> promise) {
        if (promise == null) {
            // 如果回调接口为空，说明此次RPC请求不需要返回响应消息，也不需要记录回调
            return;
        }
        // 将序列ID与回调接口缓存起来，等待消息返回之后调用
        callbackMap.put(seqId, promise);
        // 启动一个延时任务，如果到达时间还没有收到返回，抛出超时异常
        eventExecutorGroup.schedule(() -> {
            Promise<?> value = callbackMap.remove(seqId);
            if (value != null) {
                // 如果延时任务到达的时候，缓存中还存在映射，则返回超时的错误码
                value.setFailure(GameErrorException.newBuilder(GameRPCError.TIME_OUT).build());
            }
        }, timeout, TimeUnit.SECONDS);
    }

    // 收到RPC响应消息时调用此方法
    public void callback(IGameMessage gameMessage) {
        // 获取请求的序列ID
        int seqId = gameMessage.getHeader().getClientSeqId();
        // 从缓存集合中移除
        Promise<IGameMessage> promise = this.callbackMap.remove(seqId);
        if (promise != null) {
            // 调用回调方法，执行响应消息
            promise.setSuccess(gameMessage);
        }
    }
}
