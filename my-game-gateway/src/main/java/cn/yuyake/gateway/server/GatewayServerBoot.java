package cn.yuyake.gateway.server;

import cn.yuyake.gateway.server.handler.TestGameMessageHandler;
import cn.yuyake.gateway.server.handler.codec.DecodeHandler;
import cn.yuyake.gateway.server.handler.codec.EncodeHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class GatewayServerBoot {
    @Autowired // 注入网关服务配置
    private GatewayServerConfig serverConfig;

    private Logger logger = LoggerFactory.getLogger(GatewayServerBoot.class);
    private NioEventLoopGroup bossGroup = null;
    private NioEventLoopGroup workerGroup = null;

    public void startServer() {
        // bossGroup 负责监听端口，与客户端建立连接
        bossGroup = new NioEventLoopGroup(serverConfig.getBossThreadCount());
        // workGroup 用于处理网络通信之间的消息，包括编码、解码和业务逻辑（业务逻辑线程组）
        workerGroup = new NioEventLoopGroup(serverConfig.getWorkThreadCount());
        int port = serverConfig.getPort();
        try {
            // 启动类
            ServerBootstrap b = new ServerBootstrap();
            // 这里遇到一个小问题：
            // 如果把childHandler的加入放在option的前面，option将会不生效。
            // 用java socket连接，一直没有消息返回 ？？？
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(createChannelInitializer());
            logger.info("开始启动服务，端口:{}", serverConfig.getPort());
            ChannelFuture f = b.bind(port).sync(); // 阻塞
            f.channel().closeFuture().sync(); // 等待服务关闭成功
        } catch (InterruptedException e) {
            logger.error("中断异常", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /**
     * 连接channel初始化的时候调用
     */
    private ChannelInitializer<Channel> createChannelInitializer() {
        ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                // 添加编码Handler
                p.addLast("EncodeHandler", new EncodeHandler(serverConfig));
                // 添加拆包
                p.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, -4, 0));
                // 添加解码
                p.addLast("DecodeHandler", new DecodeHandler());
                // 添加业务实现
                p.addLast(new TestGameMessageHandler());
            }
        };
        return channelInitializer;
    }

    /**
     * 优雅的关闭服务
     */
    public void stop() {
        int quietPeriod = 5;
        int timeout = 30;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        workerGroup.shutdownGracefully(quietPeriod, timeout, timeUnit);
        bossGroup.shutdownGracefully(quietPeriod, timeout, timeUnit);
    }
}
