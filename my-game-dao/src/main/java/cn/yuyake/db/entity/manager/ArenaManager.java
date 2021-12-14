package cn.yuyake.db.entity.manager;

import cn.yuyake.db.entity.Arena;

public class ArenaManager {

    private final Arena arena;

    public ArenaManager(Arena arena) {
        this.arena = arena;
    }

    public Arena getArena() {
        return arena;
    }

    public void addChallengeTimes(int times) {
        int result = arena.getChallengeTimes() + times;
        arena.setChallengeTimes(result);
    }
}
