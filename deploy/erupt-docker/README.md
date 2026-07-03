### Erupt All-in-One Docker Image

This image packages a full Erupt admin application built on **`erupt-spring-boot-starter-all`**, which bundles the core admin (upms, security, web) plus every optional feature module: designer, job, generator, monitor, magic-api, websocket, notice, print, terminal, AI, and cloud-server.

### Quick Start

Zero configuration — an embedded H2 file database is used by default:

```shell
docker run -d -p 8080:8080 erupts/erupt:{replace-version}
```

Then open `http://localhost:8080` (default account: `erupt` / `erupt`).

Production setup with MySQL and Redis sessions:

```shell
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://172.17.0.1:3306/erupt" \
  -e SPRING_DATASOURCE_DRIVER_CLASS_NAME="com.mysql.cj.jdbc.Driver" \
  -e SPRING_DATASOURCE_USERNAME="root" \
  -e SPRING_DATASOURCE_PASSWORD="******" \
  -e SPRING_REDIS_HOST="172.17.0.1" \
  -e ERUPT_REDIS_SESSION="true" \
  erupts/erupt:{replace-version}
```

> Redis is only required when `ERUPT_REDIS_SESSION` is enabled or cloud-server node management is used.

#### “-e” Environment Variables (All Optional)

---

| Middleware        | Environment Variable                    | Default       | Description                           |
| ----------------- | --------------------------------------- | ------------- | ------------------------------------- |
| redis             | SPRING\_REDIS\_DATABASE                 | 0             | Redis database index                  |
| redis             | SPRING\_REDIS\_TIMEOUT                  | 10000         | Redis connection timeout (ms)         |
| redis             | SPRING\_REDIS\_HOST                     | 127.0.0.1     | Redis server host                     |
| redis             | SPRING\_REDIS\_PORT                     | 6379          | Redis server port                     |
| redis             | SPRING\_REDIS\_PASSWORD                 |               | Redis password (empty = no auth)      |
| spring-datasource | SPRING\_DATASOURCE\_URL                 | jdbc:h2:file  | JDBC connection URL                   |
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

### Build & Publish

```shell
cd deploy/erupt-docker
./deploy.sh   # mvn package → docker build → docker push erupts/erupt:<version>
```
