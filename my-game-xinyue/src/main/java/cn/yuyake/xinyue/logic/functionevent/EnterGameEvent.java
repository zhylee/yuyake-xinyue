package cn.yuyake.xinyue.logic.functionevent;

import cn.yuyake.db.entity.manager.PlayerManager;
import org.springframework.context.ApplicationEvent;

public class EnterGameEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private final PlayerManager playerManager;

    public EnterGameEvent(Object source, PlayerManager playerManager) {
        super(source);
        this.playerManager = playerManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }


}