package cn.yuyake.dao;

import cn.yuyake.db.entity.Player;
import cn.yuyake.db.repository.PlayerRepository;
import cn.yuyake.redis.EnumRedisKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public class PlayerDao extends AbstractDao<Player, Long> {
    @Autowired
    private PlayerRepository playerRepository;

    @Override
    protected EnumRedisKey getRedisKey() {
        return EnumRedisKey.PLAYER_INFO;
    }

    @Override
    protected MongoRepository<Player, Long> getMongoRepository() {
        return playerRepository;
    }

    @Override
    protected Class<Player> getEntityClass() {
        return Player.class;
    }
}
