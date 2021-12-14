package cn.yuyake.common.eventsystem;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Bean的配置类
public class BeanConfig {
    @Bean // 配置TaskService的Bean创建
    public TaskService getTaskService() {
        return new TaskService();
    }
}
