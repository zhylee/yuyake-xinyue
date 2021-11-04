package cn.yuyake.client.service.handler;

import cn.yuyake.game.messagedispatcher.DispatchGameMessageService;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO Dispatch Game Message Handler
public class DispatchGameMessageHandler extends ChannelInboundHandlerAdapter {
    private DispatchGameMessageService dispatchGameMessageService;
    private static Logger logger = LoggerFactory.getLogger(DispatchGameMessageHandler.class);
    public DispatchGameMessageHandler(DispatchGameMessageService dispatchGameMessageService) {
        this.dispatchGameMessageService = dispatchGameMessageService;
    }
}
