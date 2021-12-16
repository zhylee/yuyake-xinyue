package cn.yuyake.db.entity.manager;

import cn.yuyake.common.error.IServerError;

public enum GameErrorCode implements IServerError {
    HeroNotExist(101, "英雄不存在"),
    WeaponNotExist(102, "武器不存在"),
    HeroLevelNotEnough(103,"英雄等级不足"),
    EquipWeaponCostNotEnough(104,"装备武器消耗不足"),
    WeaponUnEnable(105,"武器不可用"),
    HeroHadEquipWeapon(106, "此英雄已装备武器"),
    ;
    private final int errorCode;
    private final String desc;

    GameErrorCode(int errorCode, String desc) {
        this.errorCode = errorCode;
        this.desc = desc;
    }

    @Override
    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorDesc() {
        return desc;
    }
}
