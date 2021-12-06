package cn.yuyake.db.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.concurrent.ConcurrentHashMap;

@Document(collection = "Player")
public class Player {

    @Id
    private long playerId;
    private String nickName;
    private long lastLoginTime;
    private long createTime;

    private ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public ConcurrentHashMap<String, Integer> getMap() {
        return map;
    }

    public void setMap(ConcurrentHashMap<String, Integer> map) {
        this.map = map;
    }
}
