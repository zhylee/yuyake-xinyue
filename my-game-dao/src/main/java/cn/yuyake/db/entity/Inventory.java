package cn.yuyake.db.entity;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 背包，这里用于模拟代码，具体实际应用根据自己的需求完善
 */
public class Inventory {
    //武器包
    private ConcurrentHashMap<String, Weapon> weaponMap = new ConcurrentHashMap<>();
    //道具包
    private ConcurrentHashMap<String, Prop> propMap = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, Weapon> getWeaponMap() {
        return weaponMap;
    }

    public void setWeaponMap(ConcurrentHashMap<String, Weapon> weaponMap) {
        this.weaponMap = weaponMap;
    }

    public ConcurrentHashMap<String, Prop> getPropMap() {
        return propMap;
    }

    public void setPropMap(ConcurrentHashMap<String, Prop> propMap) {
        this.propMap = propMap;
    }
}
