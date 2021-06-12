package cn.yuyake.http.request;

import cn.yuyake.error.GameCenterError;
import org.springframework.util.StringUtils;

public class CreatePlayerParam extends AbstractHttpRequestParam {

    private String zoneId;// 如果是分区游戏，需要传区id
    private String nickName;

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    protected void haveError() {
        if (!StringUtils.hasText(zoneId)) {
            this.error = GameCenterError.ZONE_ID_IS_EMPTY;
        } else if (!StringUtils.hasText(nickName)) {
            this.error = GameCenterError.NICKNAME_IS_EMPTY;
        } else {
            int len = nickName.length();
            if (len < 2 || len > 10) {
                this.error = GameCenterError.NICKNAME_LEN_ERROR;
            }
        }
    }
}
