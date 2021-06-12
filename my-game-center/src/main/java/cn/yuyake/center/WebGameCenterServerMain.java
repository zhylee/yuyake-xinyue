package cn.yuyake.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages= {"cn.yuyake"})
@EnableMongoRepositories(basePackages= {"cn.yuyake"})
public class WebGameCenterServerMain {
    public static void main(String[] args) {
        SpringApplication.run(WebGameCenterServerMain.class, args);
    }
}
