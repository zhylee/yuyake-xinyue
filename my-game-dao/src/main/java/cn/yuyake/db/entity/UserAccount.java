package cn.yuyake.db.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

// Document 注解标记数据对象在 MongoDB 中存储时的 Collection 的名字
@Document(collection = "UserAccount")
public class UserAccount {

    @Id // 标记为数据库主键
    private String openId; // 用户的账号ID，一般是第三方SDK的openId
    private long userId; // 用户唯一ID，由服务器自己维护，要保证全局唯一
    private long createTime; // 注册时间
    private String ip; // 注册IP
    // 记录已创建角色的基本信息
    private Map<String, Player> playerInfo = new HashMap<>();

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Map<String, Player> getPlayerInfo() {
        return playerInfo;
    }

    public void setPlayerInfo(Map<String, Player> playerInfo) {
        this.playerInfo = playerInfo;
    }
}
