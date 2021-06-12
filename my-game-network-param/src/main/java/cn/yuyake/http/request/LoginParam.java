package cn.yuyake.http.request;

import cn.yuyake.common.utils.CommonField;
import cn.yuyake.error.GameCenterError;
import org.springframework.util.StringUtils;

public class LoginParam extends AbstractHttpRequestParam {
    private String openId;
    private String token;

    private String ip;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    @Override
    protected void haveError() {
        // 验证登陆参数
        if (!StringUtils.hasLength(openId)) {
            this.error = GameCenterError.OPENID_IS_EMPTY;
        } else if (openId.length() > CommonField.OPEN_ID_LENGTH) {
            this.error = GameCenterError.OPENID_LEN_ERROR;
        } else if (!StringUtils.hasLength(token)) {
            this.error = GameCenterError.SDK_TOKEN_ERROR;
        } else if (token.length() > 128) {
            this.error = GameCenterError.SDK_TOKEN_LEN_ERROR;
        }
    }


}
