package cn.yuyake.gateway.filter;

import cn.yuyake.common.utils.CommonField;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 全局过滤器
 */
@Service
public class RequestRateLimiterFilter implements GlobalFilter, Ordered {

    @Autowired // 引用过滤器
    private FilterConfig filterConfig;
    // 针对所有用户的限流器
    private RateLimiter globalRateLimiter;
    // 单个用户的流量限制缓存
    private LoadingCache<String, RateLimiter> userRateLimiterCache;
    // 日志
    private Logger logger = LoggerFactory.getLogger(RequestRateLimiterFilter.class);

    @PostConstruct // 在服务启动的时候自动初始化
    public void init() {
        double permitsPerSecond = filterConfig.getGlobalRequestRateCount();
        globalRateLimiter = RateLimiter.create(permitsPerSecond);
        // 创建用户cache
        long maximumSize = filterConfig.getCacheUserMaxCount();
        long duration = filterConfig.getCacheUserTimeout();
        userRateLimiterCache = CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterAccess(duration, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<String, RateLimiter>() {
                    @Override
                    public RateLimiter load(String key) throws Exception {
                        // 不存在限流器就创建一个
                        double permitsPerSecond = filterConfig.getUserRequestRateCount();
                        RateLimiter newRateLimiter = RateLimiter.create(permitsPerSecond);
                        return newRateLimiter;
                    }
                });
    }

    @Override
    public int getOrder() {
        // 全局过滤器需要在token验证的过滤器后面加载
        return Ordered.LOWEST_PRECEDENCE;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 从 Http 请求 Header 中获取用户的 openId
        String openId = exchange.getRequest().getHeaders().getFirst(CommonField.OPEN_ID);
        // 如果存在 openId，判断个人限流
        if (StringUtils.hasText(openId)) {
            try {
                RateLimiter userRateLimiter = userRateLimiterCache.get(openId);
                // 获取令牌失败，触发限流
                if (!userRateLimiter.tryAcquire()) {
                    return this.tooManyRequest(exchange, chain);
                }
            } catch (ExecutionException e) {
                logger.error("limit filter error", e);
                return this.tooManyRequest(exchange, chain);
            }
        }
        // 全局限流判断
        if (!globalRateLimiter.tryAcquire()) {
            return this.tooManyRequest(exchange, chain);
        }
        // 成功获取令牌，放行
        return chain.filter(exchange);
    }

    private Mono<Void> tooManyRequest(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.debug("request too many, trigger limit!");
        // 请求失败，返回请求太多
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        // 设置请求完成，直接给客户端返回
        return exchange.getResponse().setComplete();
    }
}
