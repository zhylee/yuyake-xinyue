package cn.yuyake.gateway.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "game.gateway.server.config")
public class GatewayServerConfig {
    private int port;
    private int bossThreadCount;
    private int workThreadCount;
    // 服务器ID
    private int serverId;
    // 达到压缩的消息最小大小
    private int compressMessageSize = 1024 * 2;
    // 等待认证的超时时间
    private int waitConfirmTimeoutSecond = 30;
    // 全局流量限制请允许每秒请求数量
    private double globalRequestPerSecond = 2000;
    // 单个用户的限流请允许的每秒请求数量
    private double requestPerSecond = 10;
    // channel读取空闲时间
    private int readerIdleTimeSeconds = 300;
    // channel写出空闲时间
    private int writerIdleTimeSeconds = 12;
    // channel读写空闲时间
    private int allIdleTimeSeconds = 15;
    // 向游戏服务发送消息的Topic前缀
    private String businessGameMessageTopic = "business-game-message-topic";
    // 接收游戏服务响应消息的Topic
    private String gatewayGameMessageTopic = "gateway-game-message-topic";

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBossThreadCount() {
        return bossThreadCount;
    }

    public void setBossThreadCount(int bossThreadCount) {
        this.bossThreadCount = bossThreadCount;
    }

    public int getWorkThreadCount() {
        return workThreadCount;
    }

    public void setWorkThreadCount(int workThreadCount) {
        this.workThreadCount = workThreadCount;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getCompressMessageSize() {
        return compressMessageSize;
    }

    public void setCompressMessageSize(int compressMessageSize) {
        this.compressMessageSize = compressMessageSize;
    }

    public int getWaitConfirmTimeoutSecond() {
        return waitConfirmTimeoutSecond;
    }

    public void setWaitConfirmTimeoutSecond(int waitConfirmTimeoutSecond) {
        this.waitConfirmTimeoutSecond = waitConfirmTimeoutSecond;
    }

    public double getGlobalRequestPerSecond() {
        return globalRequestPerSecond;
    }

    public void setGlobalRequestPerSecond(double globalRequestPerSecond) {
        this.globalRequestPerSecond = globalRequestPerSecond;
    }

    public double getRequestPerSecond() {
        return requestPerSecond;
    }

    public void setRequestPerSecond(double requestPerSecond) {
        this.requestPerSecond = requestPerSecond;
    }

    public int getReaderIdleTimeSeconds() {
        return readerIdleTimeSeconds;
    }

    public void setReaderIdleTimeSeconds(int readerIdleTimeSeconds) {
        this.readerIdleTimeSeconds = readerIdleTimeSeconds;
    }

    public int getWriterIdleTimeSeconds() {
        return writerIdleTimeSeconds;
    }

    public void setWriterIdleTimeSeconds(int writerIdleTimeSeconds) {
        this.writerIdleTimeSeconds = writerIdleTimeSeconds;
    }

    public int getAllIdleTimeSeconds() {
        return allIdleTimeSeconds;
    }

    public void setAllIdleTimeSeconds(int allIdleTimeSeconds) {
        this.allIdleTimeSeconds = allIdleTimeSeconds;
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
