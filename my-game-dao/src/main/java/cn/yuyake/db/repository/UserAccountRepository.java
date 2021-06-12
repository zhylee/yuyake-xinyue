package cn.yuyake.db.repository;

import cn.yuyake.db.entity.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

// MongoRepository 是 spring-data-mongodb 提供的接口，它提供了一些现成的方法可以使用
// save：如果数据库不存在，则保存数据；如果数据库已存在，则更新数据
// findById：根据ID查询数据
public interface UserAccountRepository extends MongoRepository<UserAccount, String> {
}
