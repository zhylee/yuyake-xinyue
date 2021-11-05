package cn.yuyake.gateway;

import cn.yuyake.gateway.server.GatewayServerBoot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = {"cn.yuyake"})
public class GameGatewayMain {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(GameGatewayMain.class, args);
        // 从 spring 的上下文获取实例
        GatewayServerBoot serverBoot = context.getBean(GatewayServerBoot.class);
        // 启动服务
        serverBoot.startServer();
    }
}
