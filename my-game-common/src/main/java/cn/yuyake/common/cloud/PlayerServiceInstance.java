package cn.yuyake.common.cloud;

import io.netty.util.concurrent.Promise;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class PlayerServiceInstance implements ApplicationListener<GameChannelCloseEvent> {

    @Override
    public void onApplicationEvent(GameChannelCloseEvent event) {

    }

    // TODO 根据 player id & service id 选择合适的 server id
    public Promise<Integer> selectServerId(long playerId, int serviceId, Promise<Integer> promise) {
        return null;
    }
}
