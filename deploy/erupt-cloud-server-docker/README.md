### docker 启动方式

```shell
docker run -d \
  -p 8080:8080 \
  -e SPRING_REDIS_HOST="172.17.0.1" \
  -e ERUPT_REDIS_SESSION="true" \
  erupts/erupt-cloud-server:{替换版本号}
```

#### “-e” 环境变量信息详解（全部可选）

---

| 中间件               | 环境变量                                | 默认值           | 描述                        |
|-------------------|-------------------------------------|---------------|---------------------------|
| redis             | SPRING_REDIS_DATABASE               | 0             | redis db                  |
| redis             | SPRING_REDIS_TIMEOUT                | 10000         | redis超时时间                 |
| redis             | SPRING_REDIS_HOST                   | 127.0.0.1     | redis链接地址                 |
| redis             | SPRING_REDIS_PORT                   | 6379          | redis端口                   |
| redis             | SPRING_REDIS_PASSWORD               |               | redis密码                   |
| spring-datasource | SPRING_DATASOURCE_URL               |               | 数据库连接地址                   |
| spring-datasource | SPRING_DATASOURCE_DRIVER_CLASS_NAME | org.h2.Driver | 数据库方言                     |
| spring-datasource | SPRING_DATASOURCE_USERNAME          | sa            | 数据库连接用户名                  |
| spring-datasource | SPRING_DATASOURCE_PASSWORD          |               | 数据库连接密码                   |
| jpa               | SPRING_JPA_SHOW_SQL                 | false         | jpa是否展示sql                |
| jpa               | SPRING_JPA_GENERATE_DDL             | true          | jpa是否生成ddl                |
| erupt             | ERUPT_REDIS_SESSION                 | false         | 是否使用Redis管理会话             |
| erupt             | ERUPT_CLOUD_NAME_SPACE              | erupt-cloud:  | cloud key 命名空间            |
| erupt             | ERUPT_NODE_EXPIRE_TIME              | 60000         | node节点持久化时长，单位：ms         |
| erupt             | ERUPT_NODE_SURVIVE_CHECK_TIME       | 120000        | node节点存活检查周期，单位：ms        |
| erupt             | ERUPT_VALIDATE_ACCESS_TOKEN         | true          | 是否校验 node 节点 access-token |
