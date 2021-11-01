package cn.yuyake.gateway.balance;

import cn.yuyake.common.utils.CommonField;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.*;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

public class GameCenterLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    final String serviceId;

    ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    public GameCenterLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
                                  String serviceId) {
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.serviceId = serviceId;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next()
                .map(serviceInstances -> processInstanceResponse(request, serviceInstances));
    }

    private Response<ServiceInstance> processInstanceResponse(Request request,
                                                              List<ServiceInstance> serviceInstances) {
        if (serviceInstances.isEmpty()) {
            return new EmptyResponse();
        }
        if (serviceInstances.size() == 1) {
            return new DefaultResponse(serviceInstances.get(0));
        }
        DefaultRequestContext requestContext = (DefaultRequestContext) request.getContext();
        RequestData clientRequest = (RequestData) requestContext.getClientRequest();
        HttpHeaders headers = clientRequest.getHeaders();
        // 从 HTTP 的请求 Header 中获取用户的 token 值，作为负载均衡的 key
        String routeKey = headers.getFirst(CommonField.TOKEN);
        if (routeKey == null) {
            // 如果为空，随机分配
            int index = new Random().nextInt(serviceInstances.size());
            return new DefaultResponse(serviceInstances.get(index));
        }
        // 使用 key 的 hash 值，和服务实例数量求余，选择一个服务实例
        // TODO 需要优化：1.String 哈希值不确定；2.服务实例可能改变
        int hasCode = Math.abs(routeKey.hashCode());
        int index = hasCode % serviceInstances.size();
        return new DefaultResponse(serviceInstances.get(index));
    }
}
