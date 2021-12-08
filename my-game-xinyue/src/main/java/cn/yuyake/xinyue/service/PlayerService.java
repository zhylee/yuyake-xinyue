package cn.yuyake.xinyue.service;

import cn.yuyake.db.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class PlayerService {

    private final ConcurrentHashMap<Long, Player> playerCache = new ConcurrentHashMap<>();

    public Player getPlayer(Long playerId) {
        return playerCache.get(playerId);
    }

    public void addPlayer(Player player) {
        this.playerCache.putIfAbsent(player.getPlayerId(), player);
    }
}
