# erupt-data-jpa

Relational database data source for Erupt, built on Spring Data JPA + Hibernate. This is the default and most-featured module — connect it to any JPA-compatible database (MySQL, PostgreSQL, Oracle, H2, MariaDB, SQL Server, SQLite, etc.) and Erupt handles CRUD, pagination, search, joins, and query pushdown out of the box.

Add the JDBC driver for your database — e.g. `mysql-connector-j`, `postgresql`, `com.h2database:h2`. HikariCP is included as the connection pool.

## Configuration

Standard Spring Boot properties:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/erupt?useSSL=false
    username: root
    password: secret
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
```

## Example

```java
@Getter
@Setter
@Entity
@Table(name = "t_user")
@Erupt(name = "User")
public class User extends BaseModel {

    @EruptField(
        views = @View(title = "Name"),
        edit = @Edit(title = "Name", notNull = true, search = @Search)
    )
    private String name;

    @EruptField(
        views = @View(title = "Age"),
        edit = @Edit(title = "Age", type = EditType.NUMBER)
    )
    private Integer age;

    @EruptField(
        views = @View(title = "Created"),
        edit = @Edit(title = "Created", type = EditType.DATE)
    )
    private LocalDateTime createdAt;
}
```

`@EruptDataProcessor` is optional here — JPA is the default processor.

## Operations

Full CRUD: list / find / add / edit / delete / drill / column-query, plus JPA relationships (`@ManyToOne`, `@OneToMany`, `@ManyToMany`), Hibernate lifecycle events, transactions, and native SQL through `EruptDao`.

## LambdaQuery — type-safe queries

Inject `EruptDao` and use method references instead of string columns:

```java
@Resource
private EruptDao eruptDao;

List<User> admins = eruptDao.lambdaQuery(User.class)
        .like(User::getName, "admin")
        .gte(User::getAge, 18)
        .orderByDesc(User::getCreatedAt)
        .list();
```

## Base model

`xyz.erupt.jpa.model.BaseModel` provides an auto-generated `Long id` and `createTime` / `updateTime` timestamps. Extend it to skip the boilerplate.
