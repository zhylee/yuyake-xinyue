package cn.yuyake.game.common;

/**
 * 做为包头的扩展类，因为网关是转发消息的，有些客户端的数据也需要一起转发出去，为了扩展时，不修改协议的编码和解码，都放在这个类里面
 * 这个类在序列化为会使用json序列化
 */
public class HeaderAttribute {
    // 客户端ID
    private String clientIp;

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    @Override
    public String toString() {
        return "HeaderAttribute [clientIp=" + clientIp + "]";
    }
}
