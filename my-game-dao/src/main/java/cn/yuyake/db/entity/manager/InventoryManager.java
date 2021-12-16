package cn.yuyake.db.entity.manager;

import cn.yuyake.common.error.GameErrorException;
import cn.yuyake.db.entity.Inventory;
import cn.yuyake.db.entity.Prop;
import cn.yuyake.db.entity.Weapon;

public class InventoryManager {

    private final Inventory inventory;

    public InventoryManager(Inventory inventory) {
        this.inventory = inventory;
    }

    public Weapon getWeapon(String weaponId) {
        return inventory.getWeaponMap().get(weaponId);
    }

    public Prop getProp(String propId) {
        return inventory.getPropMap().get(propId);
    }

    public int consumeProp(String id, int count) {
        Prop prop = this.getProp(id);
        int value = prop.getCount() - count;
        prop.setCount(value);
        return value;
    }

    public void checkWeaponExist(String weaponId) {
        if (!this.inventory.getWeaponMap().containsKey(weaponId)) {
            throw GameErrorException.newBuilder(GameErrorCode.WeaponNotExist).build();
        }
    }

    public void checkWeaponHadEquip(Weapon weapon) {
        if (!weapon.isEnable()) {
            throw GameErrorException.newBuilder(GameErrorCode.WeaponUnEnable).build();
        }
    }

    public void checkItemEnough(String propId, int needCount) {
        Prop prop = this.getProp(propId);
        if (prop.getCount() < needCount) {
            throw GameErrorException.newBuilder(GameErrorCode.EquipWeaponCostNotEnough).message("需要{} {} ", prop, needCount).build();
        }
    }
}
