package cn.yuyake.xinyue.logic.event;

public class GetArenaPlayerEvent {
    private final long playerId;

    public GetArenaPlayerEvent(long playerId) {
        this.playerId = playerId;
    }

    public long getPlayerId() {
        return playerId;
    }
}
