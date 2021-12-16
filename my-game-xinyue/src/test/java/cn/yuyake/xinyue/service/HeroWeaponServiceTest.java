package cn.yuyake.xinyue.service;

import cn.yuyake.db.entity.Hero;
import cn.yuyake.db.entity.Prop;
import cn.yuyake.db.entity.Weapon;
import cn.yuyake.db.entity.manager.HeroManager;
import cn.yuyake.db.entity.manager.InventoryManager;
import cn.yuyake.db.entity.manager.PlayerManager;
import cn.yuyake.xinyue.common.DataConfigService;
import cn.yuyake.xinyue.dataconfig.EquipWeaponDataConfig;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

@SpringBootTest(classes = {HeroWeaponService.class, DataConfigService.class})
@TestExecutionListeners(listeners = MockitoTestExecutionListener.class)
public class HeroWeaponServiceTest extends AbstractTestNGSpringContextTests {
    @SpyBean
    private HeroWeaponService heroWeaponService;
    @MockBean
    private DataConfigService dataConfigService;

    @Test
    public void addHeroWeapon() {
        // 使用Mockito创建类的实例，这样创建的实例可以指定方法的返回值，用于手动根据测试需要构造数据
        PlayerManager playerManager = Mockito.mock(PlayerManager.class);
        // 返回指定的heroManager
        HeroManager heroManager = Mockito.mock(HeroManager.class);
        Mockito.when(playerManager.getHeroManager()).thenReturn(heroManager);
        // 返回指定的hero
        Hero hero = new Hero();
        String heroId = "101";
        hero.setLevel(11);
        Mockito.when(heroManager.getHero(heroId)).thenReturn(hero);
        // 返回指定的背包管理类
        InventoryManager inventoryManager = Mockito.mock(InventoryManager.class);
        Mockito.when(playerManager.getInventoryManager()).thenReturn(inventoryManager);
        // 返回指定的weapon实例
        Weapon weapon = new Weapon();
        String weaponId = "w101";
        Mockito.when(inventoryManager.getWeapon(weaponId)).thenReturn(weapon);
        // 返回指定的数据配置类
        EquipWeaponDataConfig equipWeaponDataConfig = new EquipWeaponDataConfig();
        equipWeaponDataConfig.setLevel(10);
        equipWeaponDataConfig.setCostId("201");
        equipWeaponDataConfig.setCostCount(10);
        Mockito.when(dataConfigService.getDataConfig(weaponId, EquipWeaponDataConfig.class)).thenReturn(equipWeaponDataConfig);
        // 返回指定的道具
        Prop prop = new Prop();
        prop.setCount(20);
        Mockito.when(inventoryManager.getProp(equipWeaponDataConfig.getCostId())).thenReturn(prop);
        // 调用要测试的方法
        heroWeaponService.addHeroWeapon(playerManager, heroId, weaponId);
        // 验证结果的正确性
        assertEquals(hero.getWeaponId(), weaponId);
        assertFalse(weapon.isEnable());
    }
}
