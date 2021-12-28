package cn.yuyake.im;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"cn.yuyake"})
@EnableMongoRepositories(basePackages = {"cn.yuyake"}) // 负责连接数据库
public class GameIMMain {
    public static void main(String[] args) {

    }
}
