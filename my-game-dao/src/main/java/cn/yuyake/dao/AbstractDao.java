package cn.yuyake.dao;

import cn.yuyake.redis.EnumRedisKey;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;

/**
 * 防止缓存击穿：synchronized 互斥锁
 * 防止缓存穿透：RedisDefaultValue 默认值
 * 防止缓存雪崩：Duration 过期时间
 */
public abstract class AbstractDao<Entity, ID> {

    private static final String RedisDefaultValue = "#null#";

    @Autowired
    protected StringRedisTemplate redisTemplate;

    protected abstract EnumRedisKey getRedisKey();

    protected abstract MongoRepository<Entity, ID> getMongoRepository();

    protected abstract Class<Entity> getEntityClass();

    public Optional<Entity> findById(ID id) {
        var key = this.getRedisKey().getKey(id.toString());
        var value = redisTemplate.opsForValue().get(key);
        Entity entity = null;
        if (value == null) { // 说明 redis 中没有用户信息
            // TODO 在写代码过程中遇到有很多地方要用相同字符串，可以用intern来缓存到常量池
            //  但也要注意缓存字符串不要超过StringTable默认大小。可使用 google-guava 进行优化
            //  Interner pool = Interners.newWeakInterner(); synchronized(pool.intern(str))
            //  @see java字符串常量池----intern方法总结 https://www.liangzl.com/get-article-detail-149074.html
            //  @see Java中String做为synchronized同步锁 https://www.huaweicloud.com/articles/5f0cc8c134c69cbb462770dcee5cf282.html
            key = key.intern(); // 保证字符串在常量池中
            synchronized (key) { // 这里对 openId 加锁，防止并发操作导致的缓存击穿
                value = redisTemplate.opsForValue().get(key);// 二次获取
                if (value == null) { // 如果 redis 中，还是没有值，再从数据库取
                    Optional<Entity> op = this.getMongoRepository().findById(id);
                    if (op.isPresent()) {// 如果数据库中不为空，存储到redis中
                        entity = op.get();
                        this.updateRedis(entity, id);
                    } else {
                        this.setRedisDefaultValue(key);// 设置默认值，防止缓存穿透
                    }
                } else if(value.equals(RedisDefaultValue)) {
                    value = null; // 如果取出来的是默认值，还是返回空
                }
            }
        } else if(value.equals(RedisDefaultValue)) {
            value = null; // 如果取出来的是默认值，还是返回空
        }
        if (value != null) {
            entity = JSON.parseObject(value, this.getEntityClass());
        }
        return Optional.ofNullable(entity);
    }

    // 设置默认值
    private void setRedisDefaultValue(String key) {
        Duration duration = Duration.ofMinutes(1);
        redisTemplate.opsForValue().set(key, RedisDefaultValue,duration);
    }

    // 更新数据到 redis 缓存中
    private void updateRedis(Entity entity, ID id) {
        String key = this.getRedisKey().getKey(id.toString());
        String value = JSON.toJSONString(entity);
        Duration duration = this.getRedisKey().getTimeout();
        if (duration != null) {
            // 如果有过期时间，设置过期时间
            redisTemplate.opsForValue().set(key, value, duration);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    // 把数据更新到 redis 和数据库中
    public void saveOrUpdate(Entity entity, ID id) {
        this.updateRedis(entity, id);
        this.getMongoRepository().save(entity);
    }

}
