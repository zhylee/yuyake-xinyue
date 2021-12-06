package cn.yuyake.db.repository;

import cn.yuyake.db.entity.Player;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerRepository extends MongoRepository<Player, Long> {
}
