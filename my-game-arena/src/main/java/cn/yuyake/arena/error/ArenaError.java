package cn.yuyake.arena.error;

import cn.yuyake.common.error.IServerError;

public enum ArenaError implements IServerError {
    SERVER_ERROR(101,"服务器异常"),
    ;
    private final int errorCode;
    private final String errorDesc;

    ArenaError(int errorCode, String errorDesc) {
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

