package cn.yuyake.gateway.filter;

import cn.yuyake.common.error.TokenException;
import cn.yuyake.common.utils.CommonField;
import cn.yuyake.common.utils.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * token 验证的 filter，用户登陆成功之后，以后再访问服务需要对 token 进行验
 */
@Service
public class TokenVerifyFilter implements GlobalFilter, Ordered {

    @Autowired
    private FilterConfig filterConfig;

    private final static Logger logger = LoggerFactory.getLogger(TokenVerifyFilter.class);

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var requestUri = exchange.getRequest().getURI().getPath(); // 获取请求的 URI 路径
        var whiteRequestUris = filterConfig.getWhiteRequestUri();
        if (whiteRequestUris.contains(requestUri)) {
            return chain.filter(exchange); // 如果请求的 URI 在白名单中，则跳过验证
        }
        var token = exchange.getRequest().getHeaders().getFirst(CommonField.TOKEN);
        if (!StringUtils.hasText(token)) {
            logger.debug("{} 请求验证失败,token为空", requestUri);
            // 如果权限不对，返回401状态码
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        try {
            var tokenBody = JWTUtil.getTokenBody(token);
            // 将 token 中的 userId 和 openId 放到 header 里面，转发到业务服务中，因为业务服务需要用到
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header(CommonField.OPEN_ID, tokenBody.getOpenId())
                    .header(CommonField.USER_ID, String.valueOf(tokenBody.getUserId()))
                    .build();
            ServerWebExchange newExchange = exchange.mutate().request(request).build();
            return chain.filter(newExchange);
        } catch (TokenException e) {
            logger.debug("{} 请求验证失败,token非法", requestUri);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
