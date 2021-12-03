package cn.yuyake.client.service.loginhandler;

import cn.yuyake.client.service.handler.GameClientChannelContext;
import cn.yuyake.game.message.FirstMsgResponse;
import cn.yuyake.game.message.SecondMsgResponse;
import cn.yuyake.game.message.ThirdMsgResponse;
import cn.yuyake.game.messagedispatcher.GameMessageHandler;
import cn.yuyake.game.messagedispatcher.GameMessageMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理消息类
 */
@GameMessageHandler // 注解标记，类似于 Spring MVC 中的 Controller 注解
public class TestMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(TestMessageHandler.class);

    @GameMessageMapping(FirstMsgResponse.class) // 消息标记，类似 Spring MVC 中的 RequestMapping 注解
    public void firstMessage(FirstMsgResponse response, GameClientChannelContext ctx) {
        logger.info("first msg response: {}", response.getServerTime());
    }

    @GameMessageMapping(SecondMsgResponse.class)
    public void secondMessage(SecondMsgResponse response, GameClientChannelContext ctx) {
        logger.info("second msg response: {}", response.getBodyObj().getResult1());
    }

    @GameMessageMapping(ThirdMsgResponse.class)
    public void thirdMessage(ThirdMsgResponse response, GameClientChannelContext ctx) {
        logger.info("third msg response: {}", response.getResponseBody().getValue1());
    }
}
