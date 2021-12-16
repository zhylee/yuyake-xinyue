package cn.yuyake.db.entity;

/**
 * 英雄武器
 */
public class Weapon {

    private String id;
    private boolean enable = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
