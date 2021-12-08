package cn.yuyake.xinyue;

import cn.yuyake.dao.AsyncPlayerDao;
import cn.yuyake.game.messagedispatcher.DispatchGameMessageService;
import cn.yuyake.gateway.message.context.DispatchUserEventService;
import cn.yuyake.gateway.message.context.GatewayMessageConsumerService;
import cn.yuyake.gateway.message.context.ServerConfig;
import cn.yuyake.gateway.message.handler.GameChannelIdleStateHandler;
import cn.yuyake.xinyue.common.GameBusinessMessageDispatchHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"cn.yuyake"})
@EnableMongoRepositories(basePackages = {"cn.yuyake"}) // 负责连接数据库
public class XinYueGameServerMain {
    public static void main(String[] args) {
        // 初始化 Spring Boot 环境
        ApplicationContext context = SpringApplication.run(XinYueGameServerMain.class, args);
        // 获取配置的实例
        ServerConfig serverConfig = context.getBean(ServerConfig.class);
        // 扫描此服务可以处理的消息
        DispatchGameMessageService.scanGameMessages(context, serverConfig.getServiceId(), "cn.yuyake");
        // 获取网关消息监听实例
        GatewayMessageConsumerService gatewayMessageConsumerService = context.getBean(GatewayMessageConsumerService.class);
        // 获取Player数据操作类实例
        AsyncPlayerDao playerDao = context.getBean(AsyncPlayerDao.class);
        DispatchGameMessageService dispatchGameMessageService = context.getBean(DispatchGameMessageService.class);
        DispatchUserEventService dispatchUserEventService = context.getBean(DispatchUserEventService.class);
        gatewayMessageConsumerService.start((gameChannel) -> {
            // 初始化channel
            gameChannel.getChannelPipeline().addLast(new GameChannelIdleStateHandler(60, 60, 50));
            // 启动网关监听，并初始化GameChanelHandler
            gameChannel.getChannelPipeline().addLast(new GameBusinessMessageDispatchHandler(dispatchGameMessageService, dispatchUserEventService, playerDao));
        });
    }
}
