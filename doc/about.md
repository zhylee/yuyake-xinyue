## Gradle 管理

- [如何使用Gradle管理多模块Java项目](https://zhuanlan.zhihu.com/p/372585663)
- [依赖降级和排除](https://docs.gradle.org/current/userguide/dependency_downgrade_and_exclude.html)
- [Caused by: org.apache.logging.log4j.LoggingException: log4j-slf4j-impl cannot be present with log4j-to-slf4j](https://stackoverflow.com/questions/59629214/caused-by-org-apache-logging-log4j-loggingexception-log4j-slf4j-impl-cannot-be)
- [Gradle依赖排除](https://www.zhyea.com/2018/02/08/gradle-exclude-dependencies.html)

## Spring 用法

- [SpringBoot - 配置文件application.yml使用详解（附：Profile多环境配置）](https://www.hangge.com/blog/cache/detail_2459.html)
- [基于Gradle构建，使用SpringBoot在各个场景的应用](https://github.com/liaozihong/SpringBoot-Learning)
- [Spring Cloud 文档](https://spring.io/projects/spring-cloud)

## synchronized 锁字符串

- [java字符串常量池----intern方法总结](https://www.liangzl.com/get-article-detail-149074.html)
- [Java中String做为synchronized同步锁](https://www.huaweicloud.com/articles/5f0cc8c134c69cbb462770dcee5cf282.html)

## Spring Cloud LoadBalancer

Spring Cloud Ribbon 已被弃用，在 `2020.0.0` 之后不再发布。建议使用 Spring Cloud LoadBalancer 代替：[Spring Cloud 2020.0.0 正式发布，对开发者来说意味着什么？](https://zhuanlan.zhihu.com/p/340700505)

- [Spring Cloud LoadBalancer 官方文档](https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#spring-cloud-loadbalancer)
- [Spring Cloud LoadBalancer 官方指南](https://spring.io/guides/gs/spring-cloud-loadbalancer/)
- [Spring Cloud LoadBalancer 详细示例](https://github.com/spring-cloud-samples/spring-cloud-intro-demo)

---

- [Spring Cloud Gateway + Nacos 无法发现服务](https://www.cnblogs.com/flying607/p/14657543.html)

## docker

### redis

```
docker run --name redis -p 6379:6379 -d --restart=always redis:latest redis-server --appendonly yes --requirepass "R6YMtyPGS0f968IM"

docker pull mongo:latest
docker run -itd --name mongo -p 27017:27017 mongo --auth
docker exec -it mongo mongo admin

db.createUser({ user:'admin',pwd:'admin',roles:[ { role:'userAdminAnyDatabase', db: 'admin'},"readWriteAnyDatabase"]});
db.auth('admin', 'admin')
db.createUser({ user:'my-game',pwd:'xxx123456',roles:[ { role:'readWrite', db: 'my-game'}]});
db.auth('my-game', 'xxx123456')
```

### consul

```
docker pull consul:latest
docker run -d --name consul -p 8500:8500 consul agent -dev -ui --client=0.0.0.0
```

## 其他内容

- [JWT TOKEN not javax/xml/bind](https://blog.csdn.net/fanfuqiang/article/details/116993993)