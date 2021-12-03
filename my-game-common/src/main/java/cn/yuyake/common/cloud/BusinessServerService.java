package cn.yuyake.common.cloud;

import cn.yuyake.common.error.GameErrorException;
import cn.yuyake.common.model.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BusinessServerService implements ApplicationListener<HeartbeatEvent> {

    private final Logger logger = LoggerFactory.getLogger(BusinessServerService.class);

    @Autowired // 注入服务发现客户端
    private DiscoveryClient discoveryClient;
    @Autowired // 注入Kafka客户端
    private KafkaTemplate<String, byte[]> kafkaTemplate;
    // serviceId 对应的服务器集合，一个服务可能部署到多台服务器上面，实现负载均衡
    private Map<Integer, List<ServerInfo>> serverInfos;

    @PostConstruct
    public void init() {
        this.refreshBusinessServerInfo();
    }

    public KafkaTemplate<String, byte[]> getKafkaTemplate() {
        return kafkaTemplate;
    }

    /**
     * 从服务注册中心刷新网关后面的服务列表
     */
    private void refreshBusinessServerInfo() {
        Map<Integer, List<ServerInfo>> tempServerInfoMap = new HashMap<>();
        // 读取网关后面的服务实例
        List<ServiceInstance> businessServiceInstances = discoveryClient.getInstances("game-logic");
        logger.debug("抓取游戏服务配置成功，{}", businessServiceInstances);
        businessServiceInstances.forEach(instance -> {
            int weight = this.getServerInfoWeight(instance);
            // 根据权重计算服务实例分布
            for (int i = 0; i < weight; i++) {
                ServerInfo serverInfo = this.newServerInfo(instance);
                List<ServerInfo> serverList = tempServerInfoMap.computeIfAbsent(serverInfo.getServiceId(), s -> new ArrayList<>());
                serverList.add(serverInfo);
            }
        });
        this.serverInfos = tempServerInfoMap;
    }

    /**
     * 从游戏网关列表中选择一个游戏服务实例信息返回
     */
    public ServerInfo selectServerInfo(Integer serviceId, Long playerId) {
        // 再次声明一下，防止游戏网关列表发生变化，导致数据不一致
        Map<Integer, List<ServerInfo>> serverInfoMap = this.serverInfos;
        List<ServerInfo> serverList = serverInfoMap.get(serviceId);
        if (serverList == null || serverList.size() == 0) {
            return null;
            // throw GameErrorException.newBuilder(GameCenterError.NO_GAME_GATEWAY_INFO).build();
        }
        // 负载均衡的一个算法，使用 playerId 进行 hash 和服务器数量求余
        int hashCode = Math.abs(playerId.hashCode());
        int gatewayCount = serverList.size();
        int index = hashCode % gatewayCount;
        if (index >= gatewayCount) {
            index = gatewayCount - 1;
        }
        return serverList.get(index);
    }

    private int getServerInfoWeight(ServiceInstance instance) {
        String value = instance.getMetadata().get("weight");
        if (value == null) {
            value = "1";
        }
        return Integer.parseInt(value);
    }

    private ServerInfo newServerInfo(ServiceInstance instance) {
        String serviceId = instance.getMetadata().get("serviceId");
        if (ObjectUtils.isEmpty(serviceId)) {
            throw new IllegalArgumentException(instance.getHost() + "的服务未配置 serviceId");
        }
        String serverId = instance.getMetadata().get("serverId");
        if (ObjectUtils.isEmpty(serverId)) {
            throw new IllegalArgumentException(instance.getHost() + "的服务未配置serverId");
        }
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setServiceId(Integer.parseInt(serviceId));
        serverInfo.setServerId(Integer.parseInt(serverId));
        serverInfo.setHost(instance.getHost());
        serverInfo.setPort(instance.getPort());
        return serverInfo;
    }

    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        // 接收服务注册中心的心跳事件，每发生一次心跳事件，就刷新一次服务信息
        this.refreshBusinessServerInfo();
    }

    /**
     * 判断某个服务中的serverId是否还有效
     */
    public boolean isEnableServer(int serviceId, int serverId) {
        Map<Integer, List<ServerInfo>> serverInfoMap = this.serverInfos;
        List<ServerInfo> serverInfoList = serverInfoMap.get(serviceId);
        if(serverInfoList != null) {
            return serverInfoList.stream().anyMatch(c-> c.getServerId() == serverId);
        }
        return false;
    }
}
