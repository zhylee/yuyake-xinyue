package cn.yuyake.db.entity;

import java.util.concurrent.ConcurrentHashMap;

public class Hero implements Cloneable {

    private String heroId;
    private ConcurrentHashMap<String ,HeroSkill> skillMap;
    private int level;
    private String weaponId;

    public String getHeroId() {
        return heroId;
    }

    public void setHeroId(String heroId) {
        this.heroId = heroId;
    }

    public ConcurrentHashMap<String, HeroSkill> getSkillMap() {
        return skillMap;
    }

    public void setSkillMap(ConcurrentHashMap<String, HeroSkill> skillMap) {
        this.skillMap = skillMap;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getWeaponId() {
        return weaponId;
    }

    public void setWeaponId(String weaponId) {
        this.weaponId = weaponId;
    }
}
