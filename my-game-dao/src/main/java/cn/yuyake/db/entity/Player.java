package cn.yuyake.db.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "Player")
public class Player {

    @Id
    private long playerId;
    private String nickName;
    private long lastLoginTime;
    private long createTime;
    private Map<String, String> heroes = new HashMap<>();
    private Map<String, Integer> map = new HashMap<>();

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

    public Map<String, String> getHeroes() {
        return heroes;
    }

    public void setHeroes(Map<String, String> heroes) {
        this.heroes = heroes;
    }

    public Map<String, Integer> getMap() {
        return map;
    }

    public void setMap(Map<String, Integer> map) {
        this.map = map;
    }
}
