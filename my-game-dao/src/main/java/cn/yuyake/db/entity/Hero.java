package cn.yuyake.db.entity;

import java.util.concurrent.ConcurrentHashMap;

public class Hero implements Cloneable {

    private String heroId;
    private ConcurrentHashMap<String ,HeroSkill> skillMap;

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
}
