package cn.yuyake.db.entity.manager;

import cn.yuyake.db.entity.Hero;
import cn.yuyake.db.entity.HeroSkill;
import cn.yuyake.db.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 英雄管理类
 */
public class HeroManager {
    private static final Logger logger = LoggerFactory.getLogger(HeroManager.class);
    // 角色对象，有些日志和事件记录需要这个对象
    private final Player player;
    // 英雄数据集合
    private final Map<String, Hero> heroMap;

    public HeroManager(Player player) {
        this.player = player;
        this.heroMap = player.getHeroMap();
    }

    public void addHero(Hero hero) {
        this.heroMap.put(hero.getHeroId(), hero);
    }

    public Hero getHero(String heroId) {
        Hero hero = this.heroMap.get(heroId);
        if (hero == null) {
            logger.debug("player {} 没有英雄：{}", player.getPlayerId(), heroId);
        }
        return hero;
    }

    private HeroSkill getHeroSkill(Hero hero, String skillId) {
        HeroSkill heroSkill = hero.getSkillMap().get(skillId);
        if (heroSkill == null) {
            logger.debug("player {} hero {} skill {} 不存在", player.getPlayerId(), hero.getHeroId(), skillId);
        }
        return heroSkill;
    }

    public boolean isSkillArrivalMaxLevel(String heroId, String skillId) {
        Hero hero = this.getHero(heroId);
        HeroSkill heroSkill = this.getHeroSkill(hero, skillId);
        int skillLv = heroSkill.getLevel();
        // 根据等级判断是否达到最大等级
        return skillLv >= 100;
    }
}
