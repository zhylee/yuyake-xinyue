package cn.yuyake.dao;

import cn.yuyake.db.entity.Arena;
import cn.yuyake.db.repository.ArenaRepository;
import cn.yuyake.redis.EnumRedisKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public class ArenaDao extends AbstractDao<Arena, Long> {

    @Autowired
    private ArenaRepository arenaRepository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.ARENA;
    }

    @Override
    protected MongoRepository<Arena, Long> getMongoRepository() {
        return arenaRepository;
    }

    @Override
    protected Class<Arena> getEntityClass() {
        return Arena.class;
    }
}
