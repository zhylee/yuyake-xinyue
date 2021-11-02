package cn.yuyake.gateway.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.HashMap;
import java.util.Map;

public class GlobalExceptionCatchHandler extends DefaultErrorWebExceptionHandler {

    // 创建日志 logger
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionCatchHandler.class);

    public GlobalExceptionCatchHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resources, errorProperties, applicationContext);
    }

    /**
     * 当捕获到异常之后，在这里构造返回给客户端的错误内容。
     * 这里构造的格式和用户中心服务返回的错误格式是一致的。
     * 这样方便客户端对错误信息做统一处理。
     */
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        // 获取捕获的异常信息
        Throwable error = super.getError(request);
        Map<String, Object> result = new HashMap<>();
        // 这里可以根据自己的业务需求添加不同的错误码
        result.put("code", WebGateError.UNKNOWN.getErrorCode());
        // 在枚举中定义错误信息
        result.put("data", WebGateError.UNKNOWN.getErrorDesc() + "," + error.getMessage());
        // 记录异常日志，方便服务器定位问题
        logger.error("请求url:{},{}",request.exchange().getRequest().getURI().toString(),
                WebGateError.UNKNOWN, error);
        return result;
    }

    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        // 这里正常返回消息，请客户端根据返回的code做自定义处理
        return HttpStatus.OK.value();
    }

    @Override
    protected RequestPredicate acceptsTextHtml() {
        // 这里指定客户端不接收HTML格式的信息，全部以JSON的格式返回
        return c -> false;
    }
}
