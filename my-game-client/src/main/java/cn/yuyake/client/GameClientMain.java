package cn.yuyake.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"cn.yuyake"}) // spring 基于 cn.yuyake 包扫描
public class GameClientMain {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(GameClientMain.class);
        // 客户端不需要是一个web服务
        app.setWebApplicationType(WebApplicationType.NONE);
        // 需要注意的是，由于客户端使用了 spring Shell，它会阻塞此方法，程序不会再往下执行了
        app.run(args);
        // 所以这下面就不要添加执行的代码了，添加了也不会执行
    }
}
