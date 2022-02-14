## xinyue

基于Spring Cloud实现的分布式游戏架构

### 技术选型

- Spring Cloud Consul 集成服务注册中心Consul
- Spring Cloud Gateway 实现对客户端请求的路由转发、安全验证、状态监控、服务发现、负载均衡、请求速率限制等功能
- Spring Cloud Bus 消息总线服务，基于消息队列实现；也可以用它实现内部RPC通信
- Spring Cloud Config 管理分布式系统中的配置信息
- Gradle 管理项目、打包流程、版本发布
- MongoDB 数据持久化，方便修改表结构，可以无感知添加/删除表字段；扩展方便；不考虑事务性
- Redis 内存型数据库，二级缓存、共享内存
- Kafka 消息总线通信，高吞吐、低延迟的消息中间件
- Netty 网络编程框架、网络线程池模型
- JSON/Protocol Buffers 序列化

### 目录结构

```
├── build.gradle                    // gradle 配置
├── settings.gradle                 // gradle 配置
├── gradle.properties               // gradle 配置
├── gradlew                         // gradle 脚本
├── gradlew.bat                     // gradle 脚本
├── doc                             // 知识记录
├── README.md                       // readme
├── my-game-common                  // 公共模块
├── my-game-dao                     // 对数据库操作的通用模块
├── my-game-network-param           // 包含所有网络通信数据类的通用模块
├── my-game-gateway-message-starter // 处理用户请求与RPC请求的通用模块
├── my-game-web-gateway             // Web服务器网关项目
├── my-game-center                  // 游戏服中心项目
├── my-game-gateway                 // 游戏服务器网关项目
├── my-game-xinyue                  // 游戏业务主服务项目
├── my-game-arena                   // 竞技场微服务项目
├── my-game-im                      // 世界聊天微服务项目
└── my-game-test-start              // 服务器集成测试项目
```

### 其他变化

- 使用gradle进行管理，使用原生的platform指令，而不是dependencyManagement声明整套组件的BOM
- 由于Spring Cloud的更新换代，负载均衡从ribbon切换为LoadBalancer
- [更多踩到的坑和笔记记录](doc/about.md)

### TODO

- [ ] 转换 Manager 为 Runtime Data，封装玩家在内存数据中的容器，可在该数据中存放可丢失的临时数据。