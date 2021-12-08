package cn.yuyake.xinyue.logic.event;

public class GetPlayerInfoEvent {
    private final long playerId;

    public GetPlayerInfoEvent(long playerId) {
        this.playerId = playerId;
    }

    public long getPlayerId() {
        return playerId;
    }
}
