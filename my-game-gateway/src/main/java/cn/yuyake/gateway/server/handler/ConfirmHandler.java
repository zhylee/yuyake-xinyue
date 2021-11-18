package cn.yuyake.gateway.server.handler;

import cn.yuyake.common.utils.AESUtils;
import cn.yuyake.common.utils.JWTUtil;
import cn.yuyake.common.utils.JWTUtil.TokenBody;
import cn.yuyake.common.utils.RSAUtils;
import cn.yuyake.error.GameGatewayError;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.game.message.ConfirmMsgRequest;
import cn.yuyake.game.message.ConfirmMsgResponse;
import cn.yuyake.gateway.server.ChannelService;
import cn.yuyake.gateway.server.GatewayServerConfig;
import cn.yuyake.gateway.server.handler.codec.DecodeHandler;
import cn.yuyake.gateway.server.handler.codec.EncodeHandler;
import cn.yuyake.message.GatewayMessageCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 处理连接检测与认证
 */
public class ConfirmHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(ConfirmHandler.class);
    // 注入服务器配置
    private GatewayServerConfig serverConfig;
    // 标记连接是否认证成功
    private boolean confirmSuccess = false;
    // 定时器的返回值
    private ScheduledFuture<?> future;
    // token body
    private TokenBody tokenBody;
    // Channel Service
    private ChannelService channelService;

    public ConfirmHandler(GatewayServerConfig serverConfig, ChannelService channelService) {
        this.serverConfig = serverConfig;
        this.channelService = channelService;
    }

    /**
     * 此方法会在连接建立成功 channel 注册之后调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 从配置中获取延迟时间
        int delay = serverConfig.getWaitConfirmTimeoutSecond();
        // 添加延时定时器
        future = ctx.channel().eventLoop().schedule(() -> {
            // 如果没有认证成功，则关闭
            if (!confirmSuccess) {
                ctx.close();
            }
        }, delay, TimeUnit.SECONDS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (future != null) {
            // 如果连接关闭了，取消定时检测任务
            future.cancel(true);
        }
        if (tokenBody != null) {
            // 连接断开后，移除连接
            long playerId = tokenBody.getPlayerId();
            // 调用移除，否则出现内存泄漏的问题
            this.channelService.removeChannel(playerId, ctx.channel());
        }
        // 接下来告诉下面的 Handler
        ctx.fireChannelInactive();
    }

    private void repeatedConnect() {
        if (tokenBody != null) {
            Channel existChannel = this.channelService.getChannel(tokenBody.getPlayerId());
            if (existChannel != null) {
                // 如果检测到同一个帐号创建了多个连接，则把旧连接关闭，保留新连接
                ConfirmMsgResponse response = new ConfirmMsgResponse();
                response.getHeader().setErrorCode(GameGatewayError.REPEATED_CONNECT.getErrorCode());
                GameMessagePackage returnPackage = new GameMessagePackage();
                returnPackage.setHeader(response.getHeader());
                returnPackage.setBody(response.body());
                // 在关闭之后，给这个连接返回一条提示信息，告诉客户帐号可能异地登录了
                existChannel.close();
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        GameMessagePackage gameMessagePackage = (GameMessagePackage) msg;
        int messageId = gameMessagePackage.getHeader().getMessageId();
        if (messageId == GatewayMessageCode.ConnectConfirm.getMessageId()) {
            // 如果是认证消息，在这里处理
            ConfirmMsgRequest request = new ConfirmMsgRequest();
            // 反序列化消息内容
            request.read(gameMessagePackage.getBody());
            String token = request.getBodyObj().getToken();
            ConfirmMsgResponse response = new ConfirmMsgResponse();
            // 检测 token
            if (StringUtils.hasText(token)) {
                try {
                    // 解析 token 里面的内容，如果解析失败，会抛出异常
                    tokenBody = JWTUtil.getTokenBody(token);
                    // 标记认证成功
                    this.confirmSuccess = true;
                    // 检测重复连接
                    this.repeatedConnect();
                    // 加入连接管理
                    channelService.addChannel(tokenBody.getPlayerId(), ctx.channel());
                    // 生成此连接的AES秘钥
                    String aesSecretKey = AESUtils.createSecret(tokenBody.getUserId(), tokenBody.getServerId());
                    // 将对称加密密钥分别设置到编码和解码的 Handler 中
                    DecodeHandler decodeHandler = ctx.channel().pipeline().get(DecodeHandler.class);
                    decodeHandler.setAesSecret(aesSecretKey);
                    EncodeHandler encodeHandler = ctx.channel().pipeline().get(EncodeHandler.class);
                    encodeHandler.setAesSecret(aesSecretKey);
                    byte[] clientPublicKey = this.getClientRsaPublicKey();
                    // 使用客户端的公钥加密对称加密密钥
                    byte[] encryptAesKet = RSAUtils.encryptByPublicKey(aesSecretKey.getBytes(), clientPublicKey);
                    // 返回给客户端
                    response.getBodyObj().setSecretKey(Base64Utils.encodeToString(encryptAesKet));
                    GameMessagePackage returnPackage = new GameMessagePackage();
                    returnPackage.setHeader(response.getHeader());
                    returnPackage.setBody(response.body());
                    ctx.writeAndFlush(returnPackage);
                } catch (Exception e) {
                    // 告诉客户端 token 过期，让客户端重新获取并连接
                    if (e instanceof ExpiredJwtException) {
                        response.getHeader().setErrorCode(GameGatewayError.TOKEN_EXPIRE.getErrorCode());
                        ctx.writeAndFlush(response);
                        ctx.close();
                        logger.warn("token 过期，关闭连接");
                    } else {
                        logger.error("token 解析异常，直接关闭连接", e);
                        ctx.close();
                    }
                }
            } else {
                logger.error("token 为空，直接关闭连接");
                ctx.close();
            }
        } else {
            if (!confirmSuccess) {
                logger.trace("连接未认证，不处理任务消息，关闭连接，channelId:{}", ctx.channel().id().asShortText());
                ctx.close();
            }
            // 如果不是认证消息，则向下发送消息，让后面的Handler去处理，如果不下发，后面的Handler将接收不到消息。
            ctx.fireChannelRead(msg);
        }
    }

    // 从 token 中获取客户端的公钥
    private byte[] getClientRsaPublicKey() {
        // 获取客户端的公钥字符串
        String publicKey = tokenBody.getParam()[1];
        return Base64Utils.decodeFromString(publicKey);
    }
}
