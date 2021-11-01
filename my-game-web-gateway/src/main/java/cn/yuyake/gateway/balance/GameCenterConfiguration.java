package cn.yuyake.gateway.balance;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

public class GameCenterConfiguration {

    @Bean
    public ReactorLoadBalancer<ServiceInstance> gameCenterLoadBalancer(Environment environment,
                                                                       LoadBalancerClientFactory loadBalancerClientFactory) {
        // 获取微服务名称
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        // 创建 LoadBalancer，注意这里注入的是 LazyProvider，这主要因为在注册这个 Bean 的时候
        // 相关的 Bean 可能还没有被加载注册，利用 LazyProvider 而不是直接注入所需的 Bean 防止报找不到 Bean 注入的错误
        return new GameCenterLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
    }
}
