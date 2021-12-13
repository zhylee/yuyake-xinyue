package cn.yuyake.db.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Arena")
public class Arena {
    @Id
    private long playerId;
    // 当前剩余的挑战次数
    private int challengeTimes;

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public int getChallengeTimes() {
        return challengeTimes;
    }

    public void setChallengeTimes(int challengeTimes) {
        this.challengeTimes = challengeTimes;
    }
}
