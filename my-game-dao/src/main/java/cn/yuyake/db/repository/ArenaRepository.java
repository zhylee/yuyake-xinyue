package cn.yuyake.db.repository;

import cn.yuyake.db.entity.Arena;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArenaRepository extends MongoRepository<Arena, Long> {
}
