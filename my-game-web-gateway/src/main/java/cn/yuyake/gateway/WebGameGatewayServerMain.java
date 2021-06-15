package cn.yuyake.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// @EnableDiscoveryClient 是服务发现和注册的注解。添加它之后，在启动网关的服务时，
// 就会从 Consul 获取已注册成功的服务信息，同时也会把自己的服务信息注册到 Consul 服务上面。
@EnableDiscoveryClient
@SpringBootApplication
public class WebGameGatewayServerMain {
    public static void main(String[] args) {
        SpringApplication.run(WebGameGatewayServerMain.class, args);
    }
}
