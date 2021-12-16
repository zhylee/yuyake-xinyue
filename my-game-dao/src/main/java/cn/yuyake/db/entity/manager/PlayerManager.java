package cn.yuyake.db.entity.manager;

import cn.yuyake.db.entity.Hero;
import cn.yuyake.db.entity.Player;
import cn.yuyake.db.entity.Weapon;

public class PlayerManager {
    // 声明数据对象
    private final Player player;
    // 英雄管理类
    private final HeroManager heroManager;
    // 任务管理类
    private final TaskManager taskManager;
    // 背包管理类
    private final InventoryManager inventoryManager;
    // 声明其他的管理类...

    // 初始化所有的管理类
    public PlayerManager(Player player) {
        this.player = player;
        this.heroManager = new HeroManager(player);
        this.taskManager = new TaskManager(player.getTask());
        this.inventoryManager = new InventoryManager(player.getInventory());
        // 其他的管理类...
    }

    public int addPlayerExp(int exp) {
        // TODO 添加角色经验，判断是否升级，返回升级后当前最新的等级
        return player.getLevel();
    }

    public Player getPlayer() {
        return player;
    }

    public Hero getHero(String heroId) {
        return this.heroManager.getHero(heroId);
    }

    public Weapon getWeapon(String weaponId) {
        return this.inventoryManager.getWeapon(weaponId);
    }

    public HeroManager getHeroManager() {
        return heroManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }
}
