package cn.yuyake.gateway.message.context;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="game.server.config")
public class ServerConfig {
    // 游戏服务id
    private int serviceId;
    // 游戏服务所在的服务器id
    private int serverId;
    // 业务服务监听消息的topic
    private String businessGameMessageTopic = "business-game-message-topic";
    // 网关接收消息监听的topic
    private String gatewayGameMessageTopic = "gateway-game-message-topic";

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getBusinessGameMessageTopic() {
        return businessGameMessageTopic;
    }

    public void setBusinessGameMessageTopic(String businessGameMessageTopic) {
        this.businessGameMessageTopic = businessGameMessageTopic;
    }

    public String getGatewayGameMessageTopic() {
        return gatewayGameMessageTopic;
    }

    public void setGatewayGameMessageTopic(String gatewayGameMessageTopic) {
        this.gatewayGameMessageTopic = gatewayGameMessageTopic;
    }
}
