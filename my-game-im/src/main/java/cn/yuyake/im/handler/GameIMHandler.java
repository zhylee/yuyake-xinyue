package cn.yuyake.im.handler;

import cn.yuyake.db.entity.manager.IMManager;
import cn.yuyake.gateway.message.channel.AbstractGameChannelHandlerContext;
import cn.yuyake.gateway.message.channel.GameChannelPromise;
import cn.yuyake.gateway.message.handler.AbstractGameMessageDispatchHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.springframework.context.ApplicationContext;

public class GameIMHandler extends AbstractGameMessageDispatchHandler<IMManager> {

    private IMManager imManager;

    public GameIMHandler(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected IMManager getDataManager() {
        return imManager;
    }

    @Override
    protected void initData(AbstractGameChannelHandlerContext ctx, long playerId, GameChannelPromise promise) {
        imManager = new IMManager();
        promise.setSuccess();
    }

    @Override
    protected Future<Boolean> updateToRedis(Promise<Boolean> promise) {
        promise.setSuccess(true);
        return promise;
    }

    @Override
    protected Future<Boolean> updateToDB(Promise<Boolean> promise) {
        promise.setSuccess(true);
        return promise;
    }
}
