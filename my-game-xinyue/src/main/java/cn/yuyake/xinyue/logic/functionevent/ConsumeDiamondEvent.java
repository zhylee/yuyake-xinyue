package cn.yuyake.xinyue.logic.functionevent;

import cn.yuyake.db.entity.manager.PlayerManager;
import org.springframework.context.ApplicationEvent;

public class ConsumeDiamondEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;
    private final int diamond;
    private final PlayerManager playerManager;

    public ConsumeDiamondEvent(Object source, int diamond, PlayerManager playerManager) {
        super(source);
        this.diamond = diamond;
        this.playerManager = playerManager;
    }

    public int getDiamond() {
        return diamond;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }


}