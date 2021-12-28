package cn.yuyake.test;

import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.game.messagedispatcher.DispatchGameMessageService;
import cn.yuyake.gateway.message.channel.AbstractGameChannelHandlerContext;
import cn.yuyake.gateway.message.context.GatewayMessageContext;
import cn.yuyake.gateway.message.context.ServerConfig;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.testng.Assert.fail;

/**
 * 集成测试抽象公共类
 *
 * @param <T>
 */
public class AbstractXinYueIntegrationTest<T> extends AbstractXinYueUnitTest {

    @Autowired // 消息转化到执行方法的service类。这个类在项目中是接收完客户端消息之后，在channel的pipeline中最后一个Handler中分发消息的
    private DispatchGameMessageService dispatchGameMessageService;
    @Autowired // 服务本地配置
    private ServerConfig serverConfig;
    // 消息发送的序列id
    private int seqId;

    @BeforeClass// 在测试类启动的时候，先扫描一下服务中的请求类和处理类的映射
    public void superInit() {
        // 扫描此服务可以处理的消息
        DispatchGameMessageService.scanGameMessages(applicationContext, 0, "cn.yuyake");
    }

    public void sendGameMessage(long playerId, T data, IGameMessage request, Consumer<IGameMessage> responseConsumer) {
        GameMessageHeader header = request.getHeader();
        header.setPlayerId(playerId);
        header.setClientSeqId(this.seqId++);
        header.setClientSendTime(System.currentTimeMillis());
        header.setFromServerId(serverConfig.getServerId());
        // 因为发送消息是异常的，所以这里使用CountDownLatch保证测试代码的同步性
        CountDownLatch countDownLatch = new CountDownLatch(1);
        // Mock一下Channel的上下文在doAnswer中验证结果
        AbstractGameChannelHandlerContext gameChannelHandlerContext = Mockito.mock(AbstractGameChannelHandlerContext.class);
        GatewayMessageContext<T> stx = new GatewayMessageContext<>(data, request, gameChannelHandlerContext);
        // 验证请求的返回结果
        Mockito.doAnswer(c -> {
            IGameMessage gameMessage = c.getArgument(0);
            responseConsumer.accept(gameMessage);
            // 当消息返回时，放开下面await的阻塞
            countDownLatch.countDown();
            return null;
        }).when(gameChannelHandlerContext).writeAndFlush(Mockito.any());// 对这个方法进行mock，当处理完消息，向客户端返回时，在doAnswer中验证返回结果
        // 调用处理消息的方法
        dispatchGameMessageService.callMethod(request, stx);
        try {
            // 这里阻塞30秒，等待处理结果返回
            boolean result = countDownLatch.await(30, TimeUnit.SECONDS);
            if (!result) {
                fail("请求超时，超时时间30秒，请求：" + request.getClass().getName());
            }
        } catch (InterruptedException e) {
            // 如果异常，则测试失败
            fail("测试失败", e);
        }
    }
}
