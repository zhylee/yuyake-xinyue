package cn.yuyake.common.eventsystem;

public class PlayerUpgradeLevelEvent implements IGameEventMessage {
    // 玩家ID
    private long playerId;
    // 当前等级
    private int nowLevel;
    // 升级前的等级
    private int preLevel;
    // 消耗的经验
    private int costExp;

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public int getNowLevel() {
        return nowLevel;
    }

    public void setNowLevel(int nowLevel) {
        this.nowLevel = nowLevel;
    }

    public int getPreLevel() {
        return preLevel;
    }

    public void setPreLevel(int preLevel) {
        this.preLevel = preLevel;
    }

    public int getCostExp() {
        return costExp;
    }

    public void setCostExp(int costExp) {
        this.costExp = costExp;
    }
}
