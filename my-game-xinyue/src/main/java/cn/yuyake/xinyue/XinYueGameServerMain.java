package cn.yuyake.xinyue;

import cn.yuyake.game.messagedispatcher.DispatchGameMessageService;
import cn.yuyake.xinyue.common.ServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = {"cn.yuyake"})
public class XinYueGameServerMain {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(XinYueGameServerMain.class, args);
        ServerConfig serverConfig = context.getBean(ServerConfig.class);
        DispatchGameMessageService.scanGameMessages(context, serverConfig.getServiceId(), "cn.yuyake");
    }
}
