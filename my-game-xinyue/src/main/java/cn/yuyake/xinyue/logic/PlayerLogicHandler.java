package cn.yuyake.xinyue.logic;

import cn.yuyake.db.entity.Player;
import cn.yuyake.game.message.xinyue.*;
import cn.yuyake.game.messagedispatcher.GameMessageHandler;
import cn.yuyake.game.messagedispatcher.GameMessageMapping;
import cn.yuyake.gateway.message.context.GatewayMessageContext;
import cn.yuyake.gateway.message.context.UserEvent;
import cn.yuyake.gateway.message.context.UserEventContext;
import cn.yuyake.xinyue.logic.event.GetArenaPlayerEvent;
import cn.yuyake.xinyue.logic.event.GetPlayerInfoEvent;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@GameMessageHandler
public class PlayerLogicHandler {
    private final Logger logger = LoggerFactory.getLogger(PlayerLogicHandler.class);

    @GameMessageMapping(EnterGameMsgRequest.class) // 标记要处理的请求消息
    public void enterGame(EnterGameMsgRequest request, GatewayMessageContext ctx) {
        logger.info("接收到客户端进入游戏请求：{}", request.getHeader().getPlayerId());
        EnterGameMsgResponse response = new EnterGameMsgResponse();
        response.getBodyObj().setNickname("竹溪");
        response.getBodyObj().setPlayerId(182222788);
        // 给客户端返回消息
        ctx.sendMessage(response);
    }

    @GameMessageMapping(GetPlayerByIdMsgRequest.class)
    public void getPlayerById(GetPlayerByIdMsgRequest request, GatewayMessageContext ctx) {
        long playerId = request.getBodyObj().getPlayerId();
        // 创建一个Promise实例
        DefaultPromise<Object> promise = ctx.newPromise();
        // 创建事件对象
        GetPlayerInfoEvent event = new GetPlayerInfoEvent(playerId);
        // 发送事件，并在返回的Future上面添加监听端口
        ctx.sendUserEvent(event, promise, playerId).addListener(future -> {
            if (future.isSuccess()) {
                // 如果处理成功，返回数据
                GetPlayerByIdMsgResponse response = (GetPlayerByIdMsgResponse) future.get();
                // 向客户端返回数据
                ctx.sendMessage(response);
            } else {
                logger.error("playerId {} 数据查询失败", playerId, future.cause());
            }
        });
    }

    @GameMessageMapping(GetArenaPlayerListMsgRequest.class)
    public void getArenaPlayerList(GetArenaPlayerListMsgRequest request, GatewayMessageContext ctx) {
        // 获取本次要显示的PlayerId
        List<Long> playerIds = Arrays.asList(2L, 3L, 4L);// 模拟竞技场列表playerId
        List<GetArenaPlayerListMsgResponse.ArenaPlayer> arenaPlayers = new ArrayList<>(playerIds.size());
        // 遍历所有的PlayerId，向他们对应的GameChannel发送查询事件
        playerIds.forEach(playerId -> {
            GetArenaPlayerEvent getArenaPlayerEvent = new GetArenaPlayerEvent(playerId);
            // 注意，这个promise不能放到for循环外面，一个Promise只能被setSuccess一次
            Promise<Object> promise = ctx.newPromise();
            ctx.sendUserEvent(getArenaPlayerEvent, promise, playerId).addListener(future -> {
                if (future.isSuccess()) { // 如果执行成功，获取执行的结果
                    GetArenaPlayerListMsgResponse.ArenaPlayer arenaPlayer = (GetArenaPlayerListMsgResponse.ArenaPlayer) future.get();
                    arenaPlayers.add(arenaPlayer);
                } else {
                    arenaPlayers.add(null);
                }
                if (arenaPlayers.size() == playerIds.size()) {
                    // 如果数量相等，说明所有的事件查询都已执行成功，可以返回给客户端数据了
                    List<GetArenaPlayerListMsgResponse.ArenaPlayer> result = arenaPlayers.stream().filter(Objects::nonNull).collect(Collectors.toList());
                    GetArenaPlayerListMsgResponse response = new GetArenaPlayerListMsgResponse();
                    response.getBodyObj().setArenaPlayers(result);
                    ctx.sendMessage(response);
                }
            });
        });
    }

    @UserEvent(IdleStateEvent.class) // 处理GameChannel空闲事件
    public void idleStateEvent(UserEventContext utx, IdleStateEvent event, Promise<Object> promise) {
        logger.debug("收到空闲事件：{}", event.getClass().getName());
        // Channel空闲时，关闭Channel。会自动清理GameChannel的缓存
        utx.getCtx().close();
    }
}
