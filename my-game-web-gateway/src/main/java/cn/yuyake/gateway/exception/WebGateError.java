package cn.yuyake.gateway.exception;

public enum WebGateError {
    UNKNOWN(-2, "网关服务器未知道异常"),
    ;

    private final int errorCode;
    private final String errorDesc;
    WebGateError(int errorCode, String errorDesc) {
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }
}
