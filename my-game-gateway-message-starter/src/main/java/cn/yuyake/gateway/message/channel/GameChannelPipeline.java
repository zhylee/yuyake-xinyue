package cn.yuyake.gateway.message.channel;

import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.game.common.IGameMessage;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * 主要负责管理Handler的链表
 */
public class GameChannelPipeline {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultChannelPipeline.class);
    private static final String HEAD_NAME = generateName0(HeadContext.class);
    private static final String TAIL_NAME = generateName0(TailContext.class);
    private final GameChannel channel;
    final AbstractGameChannelHandlerContext head;
    final AbstractGameChannelHandlerContext tail;

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

    // 发送写出事件，当消息发送成功时，需要调用promise.setSuccess()
    public final GameChannelFuture writeAndFlush(IGameMessage msg, GameChannelPromise promise) {
        return tail.writeAndFlush(msg, promise);
    }

    // 发送写出事件的重载方法
    public final GameChannelFuture writeAndFlush(IGameMessage msg) {
        return tail.writeAndFlush(msg);
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

        @Override
        public void channelReadRPCRequest(AbstractGameChannelHandlerContext ctx, IGameMessage msg) throws Exception {
            ctx.fireChannelReadRPCRequest(msg);
        }

        @Override
        public void writeRPCMessage(AbstractGameChannelHandlerContext ctx, IGameMessage gameMessage, Promise<IGameMessage> callback) {
            channel.unsafeSendRpcMessage(gameMessage, callback);
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
        public void channelRegister(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise) {
            promise.setSuccess();
            logger.debug("注册事件未处理");
        }

        @Override
        public void channelReadRPCRequest(AbstractGameChannelHandlerContext ctx, IGameMessage msg) throws Exception {
            try {
                logger.debug("Discarded inbound message {} that reached at the tail of the pipeline. " + "Please check your pipeline configuration.", msg);
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }
    }


}
