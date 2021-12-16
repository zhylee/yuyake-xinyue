package cn.yuyake.xinyue.service;

import cn.yuyake.common.error.GameErrorException;
import cn.yuyake.db.entity.Hero;
import cn.yuyake.db.entity.Prop;
import cn.yuyake.db.entity.Weapon;
import cn.yuyake.db.entity.manager.GameErrorCode;
import cn.yuyake.db.entity.manager.HeroManager;
import cn.yuyake.db.entity.manager.InventoryManager;
import cn.yuyake.db.entity.manager.PlayerManager;
import cn.yuyake.xinyue.common.DataConfigService;
import cn.yuyake.xinyue.dataconfig.EquipWeaponDataConfig;
import cn.yuyake.xinyue.logic.functionevent.EquipWeaponEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class HeroWeaponService {

    @Autowired
    private DataConfigService dataConfigService;
    @Autowired
    private ApplicationContext context;

    public void addHeroWeapon(PlayerManager playerManager, String heroId, String weaponId) {
        HeroManager heroManager = playerManager.getHeroManager();
        Hero hero = heroManager.getHero(heroId);
        // 判断英雄是否存在
        if (hero == null) {
            throw GameErrorException.newBuilder(GameErrorCode.HeroNotExist).build();
        }
        // 如果武器ID不为空，说明装备过了，不能重复装备
        if (hero.getWeaponId() != null) {
            throw GameErrorException.newBuilder(GameErrorCode.HeroHadEquipWeapon).build();
        }
        InventoryManager inventoryManager = playerManager.getInventoryManager();
        Weapon weapon = inventoryManager.getWeapon(weaponId);
        // 判断是否拥有要装备的武器
        if (weapon == null) {
            throw GameErrorException.newBuilder(GameErrorCode.WeaponNotExist).build();
        }
        // 判断武器是否可以装备
        if (!weapon.isEnable()) {
            throw GameErrorException.newBuilder(GameErrorCode.WeaponUnEnable).build();
        }
        EquipWeaponDataConfig equipWeaponDataConfig = this.dataConfigService.getDataConfig(weaponId, EquipWeaponDataConfig.class);
        // 判断是否达到装备此武器的等级
        if (hero.getLevel() < equipWeaponDataConfig.getLevel()) {
            throw GameErrorException.newBuilder(GameErrorCode.HeroLevelNotEnough).message("需要等级：{}", equipWeaponDataConfig.getLevel()).build();
        }
        Prop prop = inventoryManager.getProp(equipWeaponDataConfig.getCostId());
        // 判断装备消耗的材料是否足够
        if (prop.getCount() < equipWeaponDataConfig.getCostCount()) {
            throw GameErrorException.newBuilder(GameErrorCode.EquipWeaponCostNotEnough).message("需要{} {} ", equipWeaponDataConfig.getCostId(), equipWeaponDataConfig.getCostCount()).build();
        }
        inventoryManager.consumeProp(equipWeaponDataConfig.getCostId(), equipWeaponDataConfig.getCostCount());
        // 装备成功
        hero.setWeaponId(weaponId);
        weapon.setEnable(false);
        EquipWeaponEvent event = new EquipWeaponEvent(this, heroId, weaponId);
        context.publishEvent(event);
    }

    public void addHeroWeaponNew(String heroId, String weaponId, PlayerManager playerManager) {
        // 检测参数
        this.checkAddHeroWeaponParam(heroId, weaponId, playerManager);
        Hero hero = playerManager.getHero(heroId);
        Weapon weapon = playerManager.getWeapon(weaponId);
        EquipWeaponDataConfig equipWeaponDataConfig = this.dataConfigService.getDataConfig(weaponId, EquipWeaponDataConfig.class);
        // 检测条件
        this.checkAddHeroWeaponCondition(hero, weapon, playerManager, equipWeaponDataConfig);
        // 执行业务
        this.actionEquipWeapon(hero, weapon, playerManager, equipWeaponDataConfig);
    }

    private void checkAddHeroWeaponParam(String heroId, String weaponId, PlayerManager playerManager) {
        HeroManager heroManager = playerManager.getHeroManager();
        InventoryManager inventoryManager = playerManager.getInventoryManager();
        // 检测英雄是否存在
        heroManager.checkHeroExist(heroId);
        // 检测是否拥有这个武器
        inventoryManager.checkWeaponExist(weaponId);
    }

    private void checkAddHeroWeaponCondition(Hero hero, Weapon weapon, PlayerManager playerManager, EquipWeaponDataConfig equipWeaponDataConfig) {
        HeroManager heroManager = playerManager.getHeroManager();
        InventoryManager inventoryManager = playerManager.getInventoryManager();
        // 检测英雄是否已装备武器
        heroManager.checkHadEquipWeapon(hero);
        // 检测这个武器是否已装备到其它英雄身上
        inventoryManager.checkWeaponHadEquip(weapon);
        // 检测英雄等级是否足够
        heroManager.checkHeroLevelEnough(hero.getLevel(), equipWeaponDataConfig.getLevel());
        // 检测消耗的道具是足够
        inventoryManager.checkItemEnough(equipWeaponDataConfig.getCostId(), equipWeaponDataConfig.getCostCount());
    }

    private void actionEquipWeapon(Hero hero, Weapon weapon, PlayerManager playerManager, EquipWeaponDataConfig equipWeaponDataConfig) {
        InventoryManager inventoryManager = playerManager.getInventoryManager();
        inventoryManager.consumeProp(equipWeaponDataConfig.getCostId(), equipWeaponDataConfig.getCostCount());
        hero.setWeaponId(weapon.getId());
        weapon.setEnable(false);
        EquipWeaponEvent event = new EquipWeaponEvent(this, hero.getHeroId(), weapon.getId());
        context.publishEvent(event);
    }
}
