package cn.yuyake.http.response;

import cn.yuyake.common.error.IServerError;
import com.alibaba.fastjson.JSONObject;

/**
 * {
 * "code":0, // 消息返回码，如果是0表示正确返回，否则为服务器返回的错误码
 * "data":{} // 服务器返回的具体消息内容，也是JSON格式，如果code不为0则返回的是错误的描述信息
 * }
 */
public class ResponseEntity<T> {

    private int code; // 返回的消息码，如果消息正常返回，code == 0，否则返回错误码
    private T data;
    private String errorMsg; // 当 code != 0 时，这里表示错误的详细信息

    public ResponseEntity() {
    }

    /**
     * 如果有错误信息，使用这个构造方法
     */
    public ResponseEntity(IServerError code) {
        super();
        this.code = code.getErrorCode();
        this.errorMsg = code.getErrorDesc();
    }

    /**
     * 数据正常返回，使用这个方法
     */
    public ResponseEntity(T data) {
        super();
        this.data = data;
    }

    public static <T> ResponseEntity<T> parseObject(String response, Class<T> t) {
        JSONObject root = JSONObject.parseObject(response);
        int code = root.getIntValue("code");
        ResponseEntity<T> result = new ResponseEntity<>();
        if (code == 0) {
            JSONObject dataJson = root.getJSONObject("data");
            T data = dataJson.toJavaObject(t);
            result.setData(data);
        } else {
            String errorMsg = root.getString("errorMsg");
            result.setCode(code);
            result.setErrorMsg(errorMsg);
        }
        return result;

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
