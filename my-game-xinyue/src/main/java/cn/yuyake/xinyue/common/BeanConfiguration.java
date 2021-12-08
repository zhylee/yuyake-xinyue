package cn.yuyake.xinyue.common;

import cn.yuyake.common.concurrent.GameEventExecutorGroup;
import cn.yuyake.dao.AsyncPlayerDao;
import cn.yuyake.dao.PlayerDao;
import cn.yuyake.gateway.message.context.ServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class BeanConfiguration {
    @Autowired // 注入配置信息
    private ServerConfig serverConfig;
    @Autowired // 注入数据库操作类
    private PlayerDao playerDao;
    // 处理数据库请求的线程池组
    private GameEventExecutorGroup dbExecutorGroup;

    @PostConstruct // 初始化db操作的线程池组
    public void init() {
        dbExecutorGroup = new GameEventExecutorGroup(serverConfig.getDbThreads());
    }

    @Bean // 配置AsyncPlayerDao的Bean
    public AsyncPlayerDao asyncPlayerDao() {
        return new AsyncPlayerDao(dbExecutorGroup, playerDao);
    }
}
