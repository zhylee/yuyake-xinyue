package cn.yuyake.game.common;

public class GameMessageHeader implements Cloneable {
    private int messageSize;
    private int clientSeqId;
    private int messageId;
    private int serviceId;
    private long clientSendTime;
    private long serverSendTime;
    private int version;
    private int errorCode;

    @Override
    public GameMessageHeader clone() throws CloneNotSupportedException {
        GameMessageHeader newHeader = (GameMessageHeader) super.clone();
        return newHeader;
    }

    public int getClientSeqId() {
        return clientSeqId;
    }

    public void setClientSeqId(int clientSeqId) {
        this.clientSeqId = clientSeqId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public long getClientSendTime() {
        return clientSendTime;
    }

    public void setClientSendTime(long clientSendTime) {
        this.clientSendTime = clientSendTime;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(int messageSize) {
        this.messageSize = messageSize;
    }

    public long getServerSendTime() {
        return serverSendTime;
    }

    public void setServerSendTime(long serverSendTime) {
        this.serverSendTime = serverSendTime;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
