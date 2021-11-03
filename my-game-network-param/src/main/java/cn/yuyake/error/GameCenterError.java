package cn.yuyake.error;

import cn.yuyake.common.error.IServerError;

public enum GameCenterError implements IServerError {
    UNKNOWN(-1, "用户中心服务未知异常"),
    SDK_VERIFY_ERROR(1, "sdk验证错误"),
    OPENID_IS_EMPTY(2, "openId为空"),
    OPENID_LEN_ERROR(21, "openId长度不对"),
    SDK_TOKEN_ERROR(3, "SDK token错误"),
    SDK_TOKEN_LEN_ERROR(31, "sdk token 长度不对"),
    NICKNAME_EXIST(4, "昵称已存在"),
    ZONE_ID_IS_EMPTY(5, "zoneId为空"),
    NICKNAME_IS_EMPTY(6, "昵称为空"),
    NICKNAME_LEN_ERROR(7, "昵称长度不对"),
    TOKEN_FAILED(8,"token错误"),
    NO_GAME_GATEWAY_INFO(9,"没有网关信息，无法连接游戏"),
    ;

    private final int errorCode;
    private final String errorDesc;
    GameCenterError(int errorCode, String errorDesc) {
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

    @Override
    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorDesc() {
        return errorDesc;
    }

    @Override
    public String toString() {
        return "errorCode: " + errorCode + "; errorMsg: " + errorDesc;
    }
}
