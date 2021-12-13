package cn.yuyake.common.cloud;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerServiceInstance implements ApplicationListener<GameChannelCloseEvent> {

    /**
     * 缓存PlayerID对应的所有的服务的实例的id：playerID -> serviceId -> serverId
     */
    private final Map<Long, Map<Integer, Integer>> serviceInstanceMap = new ConcurrentHashMap<>();
    @Autowired
    private BusinessServerService businessServerService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    // 创建一个事件线程，操作redis的时候，使用异步
    private final EventExecutor eventExecutor = new DefaultEventExecutor();

    @Override
    public void onApplicationEvent(GameChannelCloseEvent event) {
        this.serviceInstanceMap.remove(event.getPlayerId());
    }

    /**
     * 根据 player id & service id 选择合适的 server id
     */
    public Promise<Integer> selectServerId(long playerId, int serviceId, Promise<Integer> promise) {
        // 如果在缓存中已存在，直接获取对应的serverId；如果不存在，创建缓存对象
        Integer serverId = this.serviceInstanceMap.computeIfAbsent(playerId, i -> new ConcurrentHashMap<>()).get(serviceId);
        // 检测目前这个缓存的serverId的实例是否还有效，如果有效，直接返回
        if (serverId != null && businessServerService.isEnableServer(serviceId, serverId)) {
            return promise.setSuccess(serverId);
        }
        // 如果无效，下面再重新获取
        eventExecutor.execute(() -> {
            try {
                // 从redis查找一下，是否已由别的服务计算好
                String key = this.getRedisKey(playerId);
                Object value = redisTemplate.opsForHash().get(key, String.valueOf(serviceId));
                boolean flag = true;
                if (value != null) {
                    int serverIdOfRedis = Integer.parseInt((String) value);
                    flag = businessServerService.isEnableServer(serviceId, serverIdOfRedis);
                    // 如果redis中已缓存且是有效的服务实例serverId，直接返回
                    if (flag) {
                        promise.setSuccess(serverIdOfRedis);
                        this.addLocalCache(playerId, serviceId, serverIdOfRedis);
                    }
                }
                // 如果Redis中没有缓存，或实例已失效，重新获取一个新的服务实例Id
                if (value == null || !flag) {
                    int serverId2 = this.selectServerIdAndSaveRedis(playerId, serviceId);
                    this.addLocalCache(playerId, serviceId, serverId2);
                    promise.setSuccess(serverId2);
                }
            } catch (Throwable e) {
                promise.setFailure(e);
            }
        });
        return promise;
    }

    private int selectServerIdAndSaveRedis(long playerId, int serviceId) {
        int serverId = businessServerService.selectServerInfo(serviceId, playerId).getServerId();
        this.eventExecutor.execute(() -> {
            try {
                String key = this.getRedisKey(playerId);
                this.redisTemplate.opsForHash().put(key, String.valueOf(serviceId), String.valueOf(serverId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return serverId;
    }

    // 添加到本地缓存
    private void addLocalCache(long playerId, int serviceId, int serverId) {
        this.serviceInstanceMap.get(playerId).put(serviceId, serverId);
    }

    private String getRedisKey(long playerId) {
        return "service_instance_" + playerId;
    }
}
