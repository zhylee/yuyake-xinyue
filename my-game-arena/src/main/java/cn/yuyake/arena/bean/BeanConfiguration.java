package cn.yuyake.arena.bean;

import cn.yuyake.common.concurrent.GameEventExecutorGroup;
import cn.yuyake.dao.ArenaDao;
import cn.yuyake.dao.AsyncArenaDao;
import cn.yuyake.gateway.message.context.ServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class BeanConfiguration {
    @Autowired //注入配置信息
    private ServerConfig serverConfig;
    @Autowired //注入数据库操作类
    private ArenaDao arenaDao;
    // 线程池组
    private GameEventExecutorGroup dbExecutorGroup;

    @PostConstruct
    public void init() {
        // 初始化db操作的线程池组
        dbExecutorGroup = new GameEventExecutorGroup(serverConfig.getDbThreads());
    }

    @Bean // 配置AsyncArenaDao的Bean
    public AsyncArenaDao asyncArenaDao() {
        return new AsyncArenaDao(dbExecutorGroup, arenaDao);
    }
}
