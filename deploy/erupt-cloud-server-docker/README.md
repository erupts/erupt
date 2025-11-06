### Docker Startup

```shell
docker run -d \
  -p 8080:8080 \
  -e SPRING_REDIS_HOST="172.17.0.1" \
  -e ERUPT_REDIS_SESSION="true" \
  erupts/erupt-cloud-server:{replace-version}
```

#### “-e” Environment Variables (All Optional)

---

| Middleware        | Environment Variable                    | Default       | Description                           |
| ----------------- | --------------------------------------- | ------------- | ------------------------------------- |
| redis             | SPRING\_REDIS\_DATABASE                 | 0             | Redis database index                  |
| redis             | SPRING\_REDIS\_TIMEOUT                  | 10000         | Redis connection timeout (ms)         |
| redis             | SPRING\_REDIS\_HOST                     | 127.0.0.1     | Redis server host                     |
| redis             | SPRING\_REDIS\_PORT                     | 6379          | Redis server port                     |
| redis             | SPRING\_REDIS\_PASSWORD                 |               | Redis password (empty = no auth)      |
| spring-datasource | SPRING\_DATASOURCE\_URL                 |               | JDBC connection URL                   |
| spring-datasource | SPRING\_DATASOURCE\_DRIVER\_CLASS\_NAME | org.h2.Driver | JDBC driver class                     |
| spring-datasource | SPRING\_DATASOURCE\_USERNAME            | sa            | Database username                     |
| spring-datasource | SPRING\_DATASOURCE\_PASSWORD            |               | Database password                     |
| jpa               | SPRING\_JPA\_SHOW\_SQL                  | false         | Whether JPA shows SQL statements      |
| jpa               | SPRING\_JPA\_GENERATE\_DDL              | true          | Whether JPA generates DDL             |
| erupt             | ERUPT\_REDIS\_SESSION                   | false         | Use Redis for session management      |
| erupt             | ERUPT\_CLOUD\_NAME\_SPACE               | erupt-cloud:  | Cloud key namespace                   |
| erupt             | ERUPT\_NODE\_EXPIRE\_TIME               | 60000         | Node persistence duration (ms)        |
| erupt             | ERUPT\_NODE\_SURVIVE\_CHECK\_TIME       | 120000        | Node alive check interval (ms)        |
| erupt             | ERUPT\_VALIDATE\_ACCESS\_TOKEN          | true          | Whether to validate node access-token |
