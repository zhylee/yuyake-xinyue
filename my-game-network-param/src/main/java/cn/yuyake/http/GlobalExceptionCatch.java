package cn.yuyake.http;

import cn.yuyake.common.error.GameErrorException;
import cn.yuyake.common.error.IServerError;
import cn.yuyake.error.GameCenterError;
import cn.yuyake.http.response.ResponseEntity;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常捕获
 */
@ControllerAdvice
public class GlobalExceptionCatch {

    // 记录日志
    private final static Logger logger = LoggerFactory.getLogger(GlobalExceptionCatch.class);

    @ResponseBody
    @ExceptionHandler(value = Throwable.class) // 要捕获的异常类型，和下面的参数要一致
    public ResponseEntity<JSONObject> exceptionHandler(Throwable ex) {
        IServerError error;
        if (ex instanceof GameErrorException) { // 自定义异常，可以获取异常中的信息，返回给客户端
            GameErrorException gameError = (GameErrorException) ex;
            error = gameError.getError();
            logger.error("服务器异常，{}", ex.getMessage());
        } else { // 未知异常，一般是没有主动捕获处理的系统异常
            error = GameCenterError.UNKNOWN;
            logger.error("服务器异常", ex);
        }
        JSONObject data = new JSONObject(); // 统一给客户端返回结果
        data.put("errorMsg", ex.getMessage());
        ResponseEntity<JSONObject> response = new ResponseEntity<>(error);
        response.setData(data);
        return response;
    }
}
