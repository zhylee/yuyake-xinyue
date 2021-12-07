package cn.yuyake.gateway.message.channel;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@ConfigurationProperties(prefix = "game.channel")
public class GameChannelConfig {
    // 业务服务监听消息的topic
    private String businessGameMessageTopic = "business-game-message-topic";
    // 网关接收消息监听的topic
    private String gatewayGameMessageTopic = "gateway-game-message-topic";
    // kafka消息的groupId，一个服务一个唯一的groupId
    private String topicGroupId = "defaultGroupId:" + UUID.randomUUID();

    private int workerThreads = 16;

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

    public String getTopicGroupId() {
        return topicGroupId;
    }

    public void setTopicGroupId(String topicGroupId) {
        this.topicGroupId = topicGroupId;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }
}
