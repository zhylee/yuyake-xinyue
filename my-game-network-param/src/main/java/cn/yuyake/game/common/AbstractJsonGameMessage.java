package cn.yuyake.game.common;

import com.alibaba.fastjson.JSON;

public abstract class AbstractJsonGameMessage<T> extends AbstractGameMessage {

    // 具体的参数类实例对象：所有的请求参数和响应参数，必须以对象的形式存在
    private T bodyObj;
    // 由子类返回具体的参数对象类型
    protected abstract Class<T> getBodyObjClass();

    public AbstractJsonGameMessage() {
        if (this.getBodyObjClass() != null) {
            try {
                // 在子类实例化时，同时实例化参数对象
                bodyObj = this.getBodyObjClass().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                bodyObj = null;
            }
        }
    }

    @Override
    protected byte[] encode() {
        // 使用 JSON，将参数对象序列化
        String str = JSON.toJSONString(bodyObj);
        return str.getBytes();
    }

    @Override
    protected void decode(byte[] body) {
        // 使用 JSON，将收到的数据反序列化
        String str = new String(body);
        bodyObj = JSON.parseObject(str, this.getBodyObjClass());
    }

    @Override
    protected boolean isBodyMsgNull() {
        return this.bodyObj == null;
    }

    public T getBodyObj() {
        return bodyObj;
    }

    public void setBodyObj(T bodyObj) {
        this.bodyObj = bodyObj;
    }

    @Override
    public String toString() {
        // 重写toString，方便打印日志
        String msg = this.bodyObj == null ? null : JSON.toJSONString(bodyObj);
        return "Header:" + this.getHeader() + ", " + this.getClass().getSimpleName() + "=[bodyObj=" + msg + "]";
    }
}
