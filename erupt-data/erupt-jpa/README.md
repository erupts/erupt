# erupt-jpa

Relational database data layer for Erupt, built on Spring Data JPA.

Connects `@Erupt`-annotated entities to any JPA-compatible database (MySQL, PostgreSQL, Oracle, H2, etc.). Implements the Erupt data SPI so the core engine can perform CRUD, pagination, and search without any boilerplate DAO code.

Includes `LambdaQuery` — a type-safe query builder using method references:
```java
eruptDao.lambdaQuery(User.class).like(User::getName, "admin").list();
```
