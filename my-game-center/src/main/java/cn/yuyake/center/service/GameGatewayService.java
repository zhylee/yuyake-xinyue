package cn.yuyake.center.service;

import cn.yuyake.center.dataconfig.GameGatewayInfo;
import cn.yuyake.common.error.GameErrorException;
import cn.yuyake.error.GameCenterError;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 游戏服务器网关服务类
 * <p>
 * 1. 在服务启动之后，从服务发现组件中刷新网关服务实例列表
 * 2. 实现负载均衡策略，根据角色ID（playerId）选择返回一个合适的网关信息
 * 3. 保证同一个角色ID在一定时间内获取的都是同一个网关信息
 * 4. 定时刷新网关服务实例列表，使网关服务列表保持最新状态
 * </p>
 */
@Service
public class GameGatewayService implements ApplicationListener<HeartbeatEvent> {

    private final Logger logger = LoggerFactory.getLogger(GameGatewayService.class);
    // 参与网关分配的网关集合
    private List<GameGatewayInfo> gameGatewayInfoList;
    // 用户分配的网关缓存
    private LoadingCache<Long, GameGatewayInfo> userGameGatewayCache;
    // 注入服务发现客户端实例
    @Autowired
    private DiscoveryClient discoveryClient;

    // 游戏服务中心启动之后，向Consul获取注册的游戏网关信息
    @PostConstruct
    public void init() {
        // 在服务启动成功之后，获取网关服务实例列表信息
        this.refreshGameGatewayInfo();
        // 初始化用户分配的游戏服务器网关信息缓存
        // 最大缓存数为20000，每个缓存有效期是2h TODO 这个后期可以优化到配置文件中
        userGameGatewayCache = CacheBuilder.newBuilder()
                .maximumSize(20000)
                .expireAfterAccess(2, TimeUnit.HOURS)
                .build(new CacheLoader<Long, GameGatewayInfo>() {
                    @Override
                    public GameGatewayInfo load(Long key) throws Exception {
                        // 如果不存在，从当前的网关服务列表中选择一个网关信息
                        GameGatewayInfo gameGatewayInfo = selectGameGateway(key);
                        return gameGatewayInfo;
                    }
                });
    }

    /**
     * 根据心跳事件，刷新游戏网关列表信息
     */
    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        this.refreshGameGatewayInfo();
    }


    /**
     * 刷新游戏网关列表信息
     */
    private void refreshGameGatewayInfo() {
        // 根据 service-name 获取服务信息，这里的 service-name 就是在 application.yml 中配置的 service-name
        List<ServiceInstance> gameGatewayServiceInstances = discoveryClient.getInstances("game-gateway");
        logger.debug("获取游戏服务器网关配置成功，{}", gameGatewayServiceInstances);
        List<GameGatewayInfo> initGameGatewayInfoList = new ArrayList<>();
        AtomicInteger gameGatewayId = new AtomicInteger(1); // ID自增
        gameGatewayServiceInstances.forEach(instance -> {
            int weight = this.getGameGatewayWeight(instance);
            // 根据权重初始化游戏服务器网关数量
            for (int i = 0; i < weight; i++) {
                int id = gameGatewayId.getAndIncrement();
                // 构造游戏服务器网关信息类
                GameGatewayInfo gameGatewayInfo = this.newGameGatewayInfo(id, instance);
                initGameGatewayInfoList.add(gameGatewayInfo);
            }
        });
        // 打乱顺序，让游戏服务器网关分布更均匀
        Collections.shuffle(initGameGatewayInfoList);
        initGameGatewayInfoList.forEach(i -> logger.debug(i.toString()));
        // 更新网关服务信息列表
        this.gameGatewayInfoList = initGameGatewayInfoList;
    }

    private int getGameGatewayWeight(ServiceInstance instance) {
        String value = instance.getMetadata().get("weight");
        return value == null ? 1 : Integer.parseInt(value);
    }

    private GameGatewayInfo newGameGatewayInfo(int id, ServiceInstance instance) {
        GameGatewayInfo gameGatewayInfo = new GameGatewayInfo();
        gameGatewayInfo.setId(id);
        // 网关服务注册的地址
        String ip = instance.getHost();
        // 网关中手动配置的长连接端口
        int port = this.getGameGatewayPort(instance);
        gameGatewayInfo.setIp(ip);
        gameGatewayInfo.setPort(port);
        return gameGatewayInfo;
    }

    private int getGameGatewayPort(ServiceInstance instance) {
        String value = instance.getMetadata().get("gamePort");
        if (value == null) {
            logger.warn("游戏网关{}未配置长连接端口，使用默认端口6000", instance.getServiceId());
            return 6000;
        }
        return Integer.parseInt(value);
    }

    /**
     * 从游戏网关列表中选择一个游戏网关信息返回
     */
    private GameGatewayInfo selectGameGateway(Long playerId) {
        // 再次声明一下，防止游戏网关列表发生变化，导致数据不一致
        List<GameGatewayInfo> temGameGatewayInfoList = this.gameGatewayInfoList;
        if (temGameGatewayInfoList == null || temGameGatewayInfoList.size() == 0) {
            throw GameErrorException.newBuilder(GameCenterError.NO_GAME_GATEWAY_INFO).build();
        }
        int hashCode = Math.abs(playerId.hashCode());
        int gatewayCount = temGameGatewayInfoList.size();
        int index = hashCode % gatewayCount;
        return temGameGatewayInfoList.get(index);
    }

    /**
     * 向客户端提供可以使用的游戏网关信息
     */
    public GameGatewayInfo getGameGatewayInfo(Long playerId) throws ExecutionException {
        GameGatewayInfo gameGatewayInfo = userGameGatewayCache.get(playerId);
        if (gameGatewayInfo != null) {
            List<GameGatewayInfo> gameGatewayInfos = this.gameGatewayInfoList;
            // 检测缓存的网关是否还有效，如果已被移除，从缓存中删除，并重新分配一个游戏网关信息
            if (!gameGatewayInfos.contains(gameGatewayInfo)) {
                userGameGatewayCache.invalidate(playerId);
                // 这时，缓存中已不存在playerId对应的值，会重新初始化
                gameGatewayInfo = userGameGatewayCache.get(playerId);
            }
        }
        return gameGatewayInfo;
    }
}
