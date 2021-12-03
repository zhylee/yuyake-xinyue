package cn.yuyake.client.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration // 标记为一个配置类
@ConfigurationProperties(prefix = "game.client.config") // 添加在 application.yml 配置参数的前缀
public class GameClientConfig {
    // 客户端处理数据的线程数
    private int workThreads = 16;
    // 连接超时时间，单位秒
    private int connectTimeout = 10;
    // 默认提供的游戏网关地址：localhost
    private String defaultGameGatewayHost = "localhost";
    // 默认提供的游戏网关的端口：6001
    private int defaultGameGatewayPort = 6003;
    // 是否使用服务中心，如果返回false，则使用默认游戏网关，不从服务中心获取网关信息；返回true，则从服务中心获取网关信息
    private boolean useGameCenter;
    // 游戏服务中心地址，默认是：http://localhost:5003，可以配置为网关的地址
    private String gameCenterUrl = "http://localhost:5003";
    // 消息需要压缩的大小，如果消息包大于这个值，则需要对消息进压缩
    private int messageCompressSize = 1024 * 2;
    // 客户端版本
    private int version;

    // 网关认证需要的token
    private String gatewayToken;
    // 客户端加密rsa私钥
    private String rsaPrivateKey;

    public int getWorkThreads() {
        return workThreads;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getDefaultGameGatewayHost() {
        return defaultGameGatewayHost;
    }

    public void setDefaultGameGatewayHost(String defaultGameGatewayHost) {
        this.defaultGameGatewayHost = defaultGameGatewayHost;
    }

    public int getDefaultGameGatewayPort() {
        return defaultGameGatewayPort;
    }

    public void setDefaultGameGatewayPort(int defaultGameGatewayPort) {
        this.defaultGameGatewayPort = defaultGameGatewayPort;
    }

    public boolean isUseGameCenter() {
        return useGameCenter;
    }

    public void setUseGameCenter(boolean useGameCenter) {
        this.useGameCenter = useGameCenter;
    }

    public String getGameCenterUrl() {
        return gameCenterUrl;
    }

    public void setGameCenterUrl(String gameCenterUrl) {
        this.gameCenterUrl = gameCenterUrl;
    }

    public int getMessageCompressSize() {
        return messageCompressSize;
    }

    public void setMessageCompressSize(int messageCompressSize) {
        this.messageCompressSize = messageCompressSize;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getGatewayToken() {
        return gatewayToken;
    }

    public void setGatewayToken(String gatewayToken) {
        this.gatewayToken = gatewayToken;
    }

    public String getRsaPrivateKey() {
        return rsaPrivateKey;
    }

    public void setRsaPrivateKey(String rsaPrivateKey) {
        this.rsaPrivateKey = rsaPrivateKey;
    }
}
