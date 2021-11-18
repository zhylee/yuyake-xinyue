package cn.yuyake.message;

public enum GatewayMessageCode {
    ConnectConfirm(1,"连接认证"),
    Heartbeat(2,"心跳消息"),
    ;
    private final int messageId;
    private final String desc;

    GatewayMessageCode(int messageId, String desc) {
        this.messageId = messageId;
        this.desc = desc;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getDesc() {
        return desc;
    }
}
