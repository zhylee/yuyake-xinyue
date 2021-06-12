package cn.yuyake.redis;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

import java.time.Duration;

public enum EnumRedisKey {
    USER_ID_INCR(null), // UserId 自增 key
    USER_ACCOUNT(Duration.ofDays(7)), // 用户信息
    ;
    // 此 key 的 value 的过期时间，如果为 null，表示 value 永远不过期
    private final Duration timeout;

    EnumRedisKey(Duration timeout) {
        this.timeout = timeout;
    }

    public String getKey(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("参数不能为空");
        }
        return this.name() + "_" + id;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public String getKey() {
        return this.name();
    }
}
