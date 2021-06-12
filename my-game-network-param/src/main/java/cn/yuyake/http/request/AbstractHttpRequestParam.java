package cn.yuyake.http.request;

import cn.yuyake.common.error.GameErrorException;
import cn.yuyake.common.error.IServerError;

public abstract class AbstractHttpRequestParam {
    protected IServerError error;

    public void checkParam() {
        haveError();
        if (error != null) {
            throw new GameErrorException.Builder(error).message("异常类:{}", this.getClass().getName()).build();
        }
    }
    protected abstract void haveError();
}
