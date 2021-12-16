package cn.yuyake.xinyue.logic.functionevent;

import org.springframework.context.ApplicationEvent;

public class EquipWeaponEvent extends ApplicationEvent {

    private final String heroId;
    private final String weaponId;

    public EquipWeaponEvent(Object source, String heroId, String weaponId) {
        super(source);
        this.heroId = heroId;
        this.weaponId = weaponId;
    }

    public String getHeroId() {
        return heroId;
    }

    public String getWeaponId() {
        return weaponId;
    }
}
