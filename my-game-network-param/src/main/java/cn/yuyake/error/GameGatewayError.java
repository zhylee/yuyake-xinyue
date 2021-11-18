package cn.yuyake.error;

import cn.yuyake.common.error.IServerError;

public enum GameGatewayError implements IServerError {
    TOKEN_EXPIRE(102, "TOKEN已过期"),
    REPEATED_CONNECT(103,"重复连接，可能异地登陆了"),
    ;
    private final int errorCode;
    private final String errorDesc;

    GameGatewayError(int errorCode, String errorDesc) {
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
        return "errorCode:" + this.errorCode + "; errorMsg:" + this.errorDesc;
    }
}
