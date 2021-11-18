package cn.yuyake.gateway.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "game.gateway.server.config")
public class GatewayServerConfig {
    private int port;
    private int bossThreadCount;
    private int workThreadCount;
    private int compressMessageSize = 1024 * 2; // 达到压缩的消息最小大小
    private int waitConfirmTimeoutSecond = 30; // 等待认证的超时时间

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
}
