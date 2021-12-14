package cn.yuyake.gateway.message.rpc;

import cn.yuyake.common.error.IServerError;

public enum GameRPCError implements IServerError {
    TIME_OUT(101,"RPC接收超时，没有消息返回"),
    ;
    private final int errorCode;
    private final String errorDesc;

    GameRPCError(int errorCode, String errorDesc) {
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
