## 源码编译方式演示

> 本地启动建议使用jar方式依赖部署，可降低使用成本
运行：执行`src/main/java/xyz/erupt/sample/server/EruptServerApplication.java` 文件中的main方法即可


### docker 启动方式 (推荐)

```shell
docker run -d \
  -p 8080:8080 \
  -e SPRING_REDIS_HOST="172.17.0.1" \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://172.17.0.1:3306/erupt?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai" \
  -e SPRING_DATASOURCE_PASSWORD="123qweasd" \
  --name erupt-cloud-server \
  barcke/xyz.erupt.erupt-cloud-server:{替换版本号}
```

#### ⚠️ 如果大家拉不到 docker hub的镜像的话 贴心的为大家准备了阿里云的镜像地址👇
> registry.cn-hangzhou.aliyuncs.com/barcke/xyz.erupt.erupt-cloud-server
阿里云版本 也贴心为大家准备好 shell 脚本
```shell
docker run -d \
  -p 8080:8080 \
  -e SPRING_REDIS_HOST="172.17.0.1" \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://172.17.0.1:3306/erupt?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai" \
  -e SPRING_DATASOURCE_PASSWORD="123qweasd" \
  --name erupt-cloud-server \
  registry.cn-hangzhou.aliyuncs.com/barcke/xyz.erupt.erupt-cloud-server:{替换版本号}
```

#### “-e” 环境变量信息详解（全部可选）

---

| 中间件               | 环境变量 | 案例参数                                                                                                    | 描述                                               |
|-------------------|------|---------------------------------------------------------------------------------------------------------|--------------------------------------------------|
| redis             | SPRING_REDIS_DATABASE   | 0                                                                                                       | redis db                                         |
| redis             | SPRING_REDIS_TIMEOUT                | 10000                                                                                                   | redis超时时间                                        |
| redis             | SPRING_REDIS_HOST                   | 127.0.0.1                                                                                               | redis链接地址                                        |
| redis             | SPRING_REDIS_PORT                   | 6379                                                                                                    | redis端口                                          |
| redis             | SPRING_REDIS_PASSWORD               | 123456                                                                                                  | redis密码                                          |
| spring-datasource | SPRING_DATASOURCE_URL               | jdbc:mysql://172.17.0.1:3306/erupt?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai | 数据库连接地址                                          |
| spring-datasource | SPRING_DATASOURCE_DRIVER_CLASS_NAME | com.mysql.jdbc.Driver                                                                                   | 数据库方言                                            |
| spring-datasource | SPRING_DATASOURCE_USERNAME          | root                                                                                                    | 数据库连接用户名                                         |
| spring-datasource | SPRING_DATASOURCE_PASSWORD          | 123456                                                                                                  | 数据库连接密码                                          |
| jpa               | SPRING_JPA_SHOW_SQL                 | false                                                                                                   | jpa是否展示sql                                       |
| jpa               | SPRING_JPA_GENERATE_DDL       | true                                                                                                    | jpa是否生成ddl                                       |
| jpa               | SPRING_JPA_DATABASE           | mysql                                                                                                   | 数据库                                              |
| erupt             | ERUPT_REDIS_SESSION           | true                                                                                                    | erupt是否开启redis会话                                 |
| erupt             | ERUPT_CLOUD_NAME_SPACE        | erupt-cloud:                                                                                            | cloud key 命名空间                                   |
| erupt             | ERUPT_NODE_EXPIRE_TIME        | 60000                                                                                                   | node节点持久化时长，单位：ms                                |
| erupt             | ERUPT_NODE_SURVIVE_CHECK_TIME | 120000                                                                                                  | node节点存活检查周期，单位：ms                               |
| erupt             | ERUPT_VALIDATE_ACCESS_TOKEN   | true                                                                                                    | 是否校验 node 节点 access-token，默认值 true，1.11.4及以上版本支持 |
| h2                |   SPRING_H2_CONSOLE_ENABLED   | false                                                                                                   | h2 控制台是否开启                                       |
| h2                |   SPRING_H2_CONSOLE_PATH   | /h2                                                                                                     | h2 控制台地址                                         |
| h2                |   SPRING_H2_CONSOLE_WEB_ALLOW_OTHERS   | false                                                                                                   | h2 配置                                            |

#### 版本说明

- 尾椎是 -SNAPSHOT 快照版本 不建议使用
- 尾椎是 -RELEASE 稳定版本 建议使用

| 版本号 | 说明            |
| --- |---------------|
|   1.12.17-SNAPSHOT  | 对应 1.12.17 版本 |


#### 常见问题

##### 1. Q: docker启动和宿主机连不上

A: 查找宿主机 ip 替换链接地址即可