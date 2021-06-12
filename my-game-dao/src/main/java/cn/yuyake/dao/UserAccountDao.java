package cn.yuyake.dao;

import cn.yuyake.db.entity.UserAccount;
import cn.yuyake.db.repository.UserAccountRepository;
import cn.yuyake.redis.EnumRedisKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public class UserAccountDao extends AbstractDao<UserAccount, String> {

    @Autowired
    private UserAccountRepository repository; // 注入 UserAccount 表的操作类

    // 获取唯一的用户ID
    public long getNextUserId() {
        String key = EnumRedisKey.USER_ID_INCR.getKey();
        Long userId = redisTemplate.opsForValue().increment(key);
        return userId;
    }

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.USER_ACCOUNT;
    }

    @Override
    protected MongoRepository<UserAccount, String> getMongoRepository() {
        return repository;
    }

    @Override
    protected Class<UserAccount> getEntityClass() {
        return UserAccount.class;
    }
}
