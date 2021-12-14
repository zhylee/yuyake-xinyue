package cn.yuyake.gateway.message.channel;

import cn.yuyake.common.concurrent.GameEventExecutorGroup;
import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.game.common.IGameMessage;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 主要负责管理Handler的链表
 */
public class GameChannelPipeline {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultChannelPipeline.class);
    private static final String HEAD_NAME = generateName0(HeadContext.class);
    private static final String TAIL_NAME = generateName0(TailContext.class);
    private final GameChannel channel;
    private Map<EventExecutorGroup, EventExecutor> childExecutors;
    final AbstractGameChannelHandlerContext head;
    final AbstractGameChannelHandlerContext tail;
    private static final FastThreadLocal<Map<Class<?>, String>> nameCaches = new FastThreadLocal<>() {
        @Override
        protected Map<Class<?>, String> initialValue() {
            return new WeakHashMap<>();
        }
    };

    public GameChannelPipeline(GameChannel channel) {
        this.channel = ObjectUtil.checkNotNull(channel, "channel");
        tail = new TailContext(this);
        head = new HeadContext(this);
        head.next = tail;
        tail.prev = head;
    }

    public final GameChannel gameChannel() {
        return channel;
    }

    // 发送注册事件
    public final GameChannelPipeline fireRegister(long playerId, GameChannelPromise promise) {
        AbstractGameChannelHandlerContext.invokeChannelRegistered(head, playerId, promise);
        return this;
    }

    // 发送失效事件
    public final GameChannelPipeline fireChannelInactive() {
        AbstractGameChannelHandlerContext.invokeChannelInactive(head);
        return this;
    }

    // 发送异常事件
    public final GameChannelPipeline fireExceptionCaught(Throwable cause) {
        AbstractGameChannelHandlerContext.invokeExceptionCaught(head, cause);
        return this;
    }

    // 发送用户自定义事件
    public final GameChannelPipeline fireUserEventTriggered(Object event, Promise<Object> promise) {
        AbstractGameChannelHandlerContext.invokeUserEventTriggered(head, event, promise);
        return this;
    }

    // 发送读取消息的事件
    public final GameChannelPipeline fireChannelRead(Object msg) {
        AbstractGameChannelHandlerContext.invokeChannelRead(head, msg);
        return this;
    }


    public final GameChannelPipeline fireChannelReadRPCRequest(IGameMessage gameMessage) {
        AbstractGameChannelHandlerContext.invokeChannelReadRPCRequest(head, gameMessage);
        return this;
    }

    // 发送写出事件，当消息发送成功时，需要调用promise.setSuccess()
    public final GameChannelFuture writeAndFlush(IGameMessage msg, GameChannelPromise promise) {
        return tail.writeAndFlush(msg, promise);
    }

    // 发送写出事件的重载方法
    public final GameChannelFuture writeAndFlush(IGameMessage msg) {
        return tail.writeAndFlush(msg);
    }

    public final GameChannelPipeline addLast(GameChannelHandler... handlers) {
        return addLast(null, false, handlers);
    }

    public final GameChannelPipeline addLast(GameEventExecutorGroup executor, boolean singleEventExecutorPerGroup, GameChannelHandler... handlers) {
        if (handlers == null) {
            throw new NullPointerException("handlers");
        }
        for (GameChannelHandler h : handlers) {
            if (h == null) {
                break;
            }
            addLast(executor, singleEventExecutorPerGroup, null, h);
        }
        return this;
    }

    public final GameChannelPipeline addLast(GameEventExecutorGroup group, boolean singleEventExecutorPerGroup, String name, GameChannelHandler handler) {
        final AbstractGameChannelHandlerContext newCtx;
        synchronized (this) {
            newCtx = newContext(group, singleEventExecutorPerGroup, filterName(name, handler), handler);
            addLast0(newCtx);
        }
        return this;
    }

    private void addLast0(AbstractGameChannelHandlerContext newCtx) {
        AbstractGameChannelHandlerContext prev = tail.prev;
        newCtx.prev = prev;
        newCtx.next = tail;
        prev.next = newCtx;
        tail.prev = newCtx;
    }

    // 创建一个实例
    private AbstractGameChannelHandlerContext newContext(GameEventExecutorGroup group, boolean singleEventExecutorPerGroup, String name, GameChannelHandler handler) {
        return new DefaultGameChannelHandlerContext(this, childExecutor(group, singleEventExecutorPerGroup), name, handler);
    }

    private EventExecutor childExecutor(GameEventExecutorGroup group, boolean singleEventExecutorPerGroup) {
        if (group == null) {
            return null;
        }

        if (!singleEventExecutorPerGroup) {
            return group.next();
        }
        Map<EventExecutorGroup, EventExecutor> childExecutors = this.childExecutors;
        if (childExecutors == null) {
            // Use size of 4 as most people only use one extra EventExecutor.
            childExecutors = this.childExecutors = new IdentityHashMap<>(4);
        }
        // Pin one of the child executors once and remember it so that the same child executor
        // is used to fire events for the same channel.
        EventExecutor childExecutor = childExecutors.get(group);
        if (childExecutor == null) {
            childExecutor = group.next();
            childExecutors.put(group, childExecutor);
        }
        return childExecutor;
    }

    private String filterName(String name, GameChannelHandler handler) {
        if (name == null) {
            return generateName(handler);
        }
        if (context0(name) != null) {
            throw new IllegalArgumentException("Duplicate handler name: " + name);
        }
        return name;
    }

    private String generateName(GameChannelHandler handler) {
        Map<Class<?>, String> cache = nameCaches.get();
        Class<?> handlerType = handler.getClass();
        String name = cache.get(handlerType);
        if (name == null) {
            name = generateName0(handlerType);
            cache.put(handlerType, name);
        }
        // It's not very likely for a user to put more than one handler of the same type, but make sure to
        // avoid
        // any name conflicts. Note that we don't cache the names generated here.
        if (context0(name) != null) {
            String baseName = name.substring(0, name.length() - 1); // Strip the trailing '0'.
            for (int i = 1;; i++) {
                String newName = baseName + i;
                if (context0(newName) == null) {
                    name = newName;
                    break;
                }
            }
        }
        return name;
    }

    private AbstractGameChannelHandlerContext context0(String name) {
        AbstractGameChannelHandlerContext context = head.next;
        while (context != tail) {
            if (context.name().equals(name)) {
                return context;
            }
            context = context.next;
        }
        return null;
    }

    private static String generateName0(Class<?> handlerType) {
        return StringUtil.simpleClassName(handlerType) + "#0";
    }

    final class HeadContext extends AbstractGameChannelHandlerContext implements GameChannelOutboundHandler, GameChannelInboundHandler {
        HeadContext(GameChannelPipeline pipeline) {
            super(pipeline, null, HEAD_NAME, false, true);
        }

        @Override
        public GameChannelHandler handler() {
            return this;
        }

        @Override
        public void writeAndFlush(AbstractGameChannelHandlerContext ctx, IGameMessage gameMessage, GameChannelPromise promise) throws Exception {
            GameMessagePackage gameMessagePackage = new GameMessagePackage();
            GameMessageHeader header = gameMessage.getHeader().clone();
            // 重新设置playerId，防止不同channel之间由于使用同一个IGameMessage实例，相互覆盖
            header.setPlayerId(channel.getPlayerId());
            header.setToServerId(channel.getGatewayServerId());
            header.setFromServerId(channel.getServerConfig().getServerId());
            header.setServerSendTime(System.currentTimeMillis());
            gameMessagePackage.setHeader(header);
            gameMessagePackage.setBody(gameMessage.body());
            // 调用GameChannel的方法，向外部发送消息
            channel.unsafeSendMessage(gameMessagePackage, promise);
        }

        @Override
        public void writeRPCMessage(AbstractGameChannelHandlerContext ctx, IGameMessage gameMessage, Promise<IGameMessage> callback) {
            channel.unsafeSendRpcMessage(gameMessage, callback);
        }

        @Override
        public void exceptionCaught(AbstractGameChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.fireExceptionCaught(cause);
        }

        @Override
        public void channelInactive(AbstractGameChannelHandlerContext ctx) throws Exception {
            ctx.fireChannelInactive();
        }

        @Override
        public void channelRead(AbstractGameChannelHandlerContext ctx, Object msg) throws Exception {
            ctx.fireChannelRead(msg);
        }

        @Override
        public void channelReadRPCRequest(AbstractGameChannelHandlerContext ctx, IGameMessage msg) throws Exception {
            ctx.fireChannelReadRPCRequest(msg);
        }

        @Override
        public void userEventTriggered(AbstractGameChannelHandlerContext ctx, Object evt, Promise<Object> promise) throws Exception {
            ctx.fireUserEventTriggered(evt, promise);
        }

        @Override
        public void channelRegister(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise) {
            ctx.fireChannelRegistered(playerId, promise);
        }

        @Override
        public void close(AbstractGameChannelHandlerContext ctx, GameChannelPromise promise) {
            channel.unsafeClose();
        }
    }

    final class TailContext extends AbstractGameChannelHandlerContext implements GameChannelInboundHandler {
        TailContext(GameChannelPipeline pipeline) {
            super(pipeline, null, TAIL_NAME, true, false);
        }

        @Override
        public GameChannelHandler handler() {
            return this;
        }

        @Override
        public void channelInactive(AbstractGameChannelHandlerContext ctx) throws Exception {

        }


        @Override
        public void userEventTriggered(AbstractGameChannelHandlerContext ctx, Object evt, Promise<Object> promise) throws Exception {

        }

        @Override
        public void exceptionCaught(AbstractGameChannelHandlerContext ctx, Throwable cause) throws Exception {
            try {
                logger.warn("An exceptionCaught() event was fired, and it reached at the tail of the pipeline. " + "It usually means the last handler in the pipeline did not handle the exception.", cause);
            } finally {
                ReferenceCountUtil.release(cause);
            }
        }

        @Override
        public void channelRead(AbstractGameChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                logger.debug("Discarded inbound message {} that reached at the tail of the pipeline. " + "Please check your pipeline configuration.", msg);
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelReadRPCRequest(AbstractGameChannelHandlerContext ctx, IGameMessage msg) throws Exception {
            try {
                logger.debug("Discarded inbound message {} that reached at the tail of the pipeline. " + "Please check your pipeline configuration.", msg);
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelRegister(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise) {
            promise.setSuccess();
            logger.debug("注册事件未处理");
        }
    }


}
