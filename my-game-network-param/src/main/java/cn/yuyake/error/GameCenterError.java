package cn.yuyake.error;

import cn.yuyake.common.error.IServerError;

public enum GameCenterError implements IServerError {
    UNKNOWN(-1, "用户中心服务未知异常"),
    SDK_VERIFY_ERROR(1, "sdk验证错误"),
    TOKEN_FAILED(8,"token错误"),
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
