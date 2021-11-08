package cn.yuyake.client.command;

import cn.yuyake.client.service.GameClientBoot;
import cn.yuyake.client.service.GameClientConfig;
import cn.yuyake.game.message.FirstMsgRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * 用于接收用户输入的指令，它根据 Spring Shell 的规则实现
 */
@ShellComponent
public class GameClientCommand {
    @Autowired
    private GameClientBoot gameClientBoot;
    @Autowired
    private GameClientConfig gameClientConfig;

    private final Logger logger = LoggerFactory.getLogger(GameClientCommand.class);

    /**
     * 连接服务器
     */
    @ShellMethod("连接服务器，格式：connect-server [host] [port]")
    public void connectServer(
            @ShellOption(defaultValue = "") String host,
            @ShellOption(defaultValue = "0") int port
    ) {
        // 如果默认的 host 不为空，说明是连接指定的 host
        // 如果没有指定 host，使用配置中的默认 host 和端口
        if (!host.isEmpty()) {
            if(port == 0) {
                logger.error("请输入服务器端口号");
                return;
            }
            gameClientConfig.setDefaultGameGatewayHost(host);
            gameClientConfig.setDefaultGameGatewayPort(port);
        }
        // 启动客户端并连接游戏网关
        gameClientBoot.launch();
    }

    @ShellMethod("发送测试消息，格式：send-test-msg 消息号")
    public void sendTestMsg(int messageId) {
        if(messageId == 10001) {
            // 向服务器发送一条消息
            FirstMsgRequest request = new FirstMsgRequest();
            request.setValue("Hello, server !!");
            request.getHeader().setClientSendTime(System.currentTimeMillis());
            gameClientBoot.getChannel().writeAndFlush(request);
        }
    }
}
