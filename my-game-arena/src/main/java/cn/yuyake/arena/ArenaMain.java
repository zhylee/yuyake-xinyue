package cn.yuyake.arena;

import cn.yuyake.arena.handler.ArenaGatewayHandler;
import cn.yuyake.game.messagedispatcher.DispatchGameMessageService;
import cn.yuyake.gateway.message.context.GatewayMessageConsumerService;
import cn.yuyake.gateway.message.context.ServerConfig;
import cn.yuyake.gateway.message.handler.GameChannelIdleStateHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"cn.yuyake"})
@EnableMongoRepositories(basePackages = {"cn.yuyake"})
public class ArenaMain {

    public static void main(String[] args) {
        // 初始化spring boot环境
        ApplicationContext context = SpringApplication.run(ArenaMain.class, args);
        // 获取配置的实例
        ServerConfig serverConfig = context.getBean(ServerConfig.class);
        // 扫描此服务可以处理的消息
        DispatchGameMessageService.scanGameMessages(context, serverConfig.getServiceId(), "cn.yuyake");
        // 获取网关消息监听实例
        GatewayMessageConsumerService gatewayMessageConsumerService = context.getBean(GatewayMessageConsumerService.class);
        // 启动网关消息监听，并初始化GameChannelHandler
        gatewayMessageConsumerService.start((gameChannel) -> {
            // 初始化channel
            gameChannel.getChannelPipeline().addLast(new GameChannelIdleStateHandler(120, 120, 100));
            gameChannel.getChannelPipeline().addLast(new ArenaGatewayHandler(context));
        }, serverConfig.getServerId());
    }
}
