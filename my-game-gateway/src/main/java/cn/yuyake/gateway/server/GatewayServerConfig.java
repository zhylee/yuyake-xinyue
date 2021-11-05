package cn.yuyake.gateway.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "game.gateway.server.config")
public class GatewayServerConfig {
    // 达到压缩的消息最小大小
    private int compressMessageSize = 1024 * 2;

    public int getCompressMessageSize() {
        return compressMessageSize;
    }

    public void setCompressMessageSize(int compressMessageSize) {
        this.compressMessageSize = compressMessageSize;
    }
}
