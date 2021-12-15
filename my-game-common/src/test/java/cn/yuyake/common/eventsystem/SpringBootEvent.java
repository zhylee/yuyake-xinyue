package cn.yuyake.common.eventsystem;

import org.springframework.context.ApplicationEvent;

public class SpringBootEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;
    // 自定义一些事件的信息
    private long playerId;
    private int level;
    private String reason;

    // 这个是事件源，一般是发布事件的对象实例
    public SpringBootEvent(Object source) {
        super(source);
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
