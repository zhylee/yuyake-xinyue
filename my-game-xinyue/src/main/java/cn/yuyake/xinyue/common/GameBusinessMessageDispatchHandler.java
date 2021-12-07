package cn.yuyake.xinyue.common;

import cn.yuyake.dao.PlayerDao;
import cn.yuyake.db.entity.Player;
import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.game.messagedispatcher.DispatchGameMessageService;
import cn.yuyake.gateway.message.channel.AbstractGameChannelHandlerContext;
import cn.yuyake.gateway.message.channel.GameChannelInboundHandler;
import cn.yuyake.gateway.message.channel.GameChannelPromise;
import cn.yuyake.gateway.message.context.GatewayMessageContext;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameBusinessMessageDispatchHandler implements GameChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(GameBusinessMessageDispatchHandler.class);

    private final DispatchGameMessageService dispatchGameMessageService;
    private final PlayerDao playerDao;
    private Player player;

    public GameBusinessMessageDispatchHandler(DispatchGameMessageService dispatchGameMessageService, PlayerDao playerDao) {
        this.dispatchGameMessageService = dispatchGameMessageService;
        this.playerDao = playerDao;
    }

    @Override // 在用户GameChannel注册的时候，对用户的数据进行初始化
    public void channelRegister(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise) {
        player = playerDao.findById(playerId).orElse(null);
        if (player == null) {
            logger.error("player {} 不存在", playerId);
            promise.setFailure(new IllegalArgumentException("找不到Player数据，playerId：" + playerId));
        } else {
            promise.setSuccess();
        }
    }

    @Override
    public void exceptionCaught(AbstractGameChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("服务器异常，playerId：{}", ctx.gameChannel().getPlayerId(), cause);
    }

    @Override
    public void channelInactive(AbstractGameChannelHandlerContext ctx) throws Exception {
        logger.debug("game channel 移除，playerId：{}", ctx.gameChannel().getPlayerId());
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

    }
}
