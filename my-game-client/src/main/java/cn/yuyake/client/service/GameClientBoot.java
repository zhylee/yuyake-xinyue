package cn.yuyake.client.service;

import cn.yuyake.client.service.handler.DispatchGameMessageHandler;
import cn.yuyake.client.service.handler.TestGameMessageHandler;
import cn.yuyake.client.service.handler.codec.DecodeHandler;
import cn.yuyake.client.service.handler.codec.EncodeHandler;
import cn.yuyake.client.service.handler.codec.ResponseHandler;
import cn.yuyake.game.GameMessageService;
import cn.yuyake.game.messagedispatcher.DispatchGameMessageService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameClientBoot {

    private final Logger logger = LoggerFactory.getLogger(GameClientBoot.class);
    @Autowired // 客户端配置
    private GameClientConfig gameClientConfig;
    @Autowired
    private GameMessageService gameMessageService;
    @Autowired
    private DispatchGameMessageService dispatchGameMessageService;

    private Bootstrap bootstrap;
    private EventLoopGroup eventGroup;
    private Channel channel;

    public void launch() {
        // 从配置中获取处理业务的线程数
        eventGroup = new NioEventLoopGroup(gameClientConfig.getWorkThreads());
        bootstrap = new Bootstrap();
        bootstrap.group(eventGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, gameClientConfig.getConnectTimeout() * 1000)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        // 添加编码
                        ch.pipeline().addLast("EncodeHandler", new EncodeHandler(gameClientConfig));
                        // 添加拆包
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024 * 1024 * 4, 0, 4, -4, 0));
                        // 添加解码
                        ch.pipeline().addLast("DecodeHandler", new DecodeHandler());
                        // 将响应消息转化为对应的响应对象
                        ch.pipeline().addLast("responseHandler", new ResponseHandler(gameMessageService));
                        // 添加逻辑处理
                        ch.pipeline().addLast(new DispatchGameMessageHandler(dispatchGameMessageService));
                        // 测试 Handler
                        // ch.pipeline().addLast(new TestGameMessageHandler());
                    }
                });
        ChannelFuture future = bootstrap.connect(gameClientConfig.getDefaultGameGatewayHost(),
                gameClientConfig.getDefaultGameGatewayPort());
        channel = future.channel();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.debug("连接{}:{}成功,channelId:{}", gameClientConfig.getDefaultGameGatewayHost(),
                            gameClientConfig.getDefaultGameGatewayPort(), future.channel().id().asShortText());

                } else {
                    Throwable e = future.cause();
                    logger.error("连接失败", e);
                }
            }
        });
    }

    public Channel getChannel() {
        return channel;
    }
}
