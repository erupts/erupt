# erupt-data-jdbc

Plain JDBC data source for Erupt. Bind a `@Erupt` model to a single database table without JPA / entity mapping — use it when you don't need Hibernate, want to point at a legacy table, or need to talk to a database (ClickHouse, Doris, TDengine, TiDB, DM, Kingbase, etc.) that has a JDBC driver.

Filtering, sorting, and paging are pushed down as SQL through Spring's `NamedParameterJdbcTemplate`. Condition values are always bound as named parameters; condition keys and sort fields must be declared model fields — both guard against SQL injection.

Add the JDBC driver for your database. A `DataSource` bean is required — either the Spring Boot auto-configured one from `spring.datasource.*` or a named bean.

## Annotation

`@EruptJdbc`

| Attribute    | Default | Description                                                 |
|--------------|---------|-------------------------------------------------------------|
| `value`      | —       | Table name                                                  |
| `datasource` | `""`    | Bean name of an alternate `DataSource`; empty = the primary |

## Example

```java
@Getter
@Setter
@Erupt(name = "Order", primaryKeyCol = "id")
@EruptJdbc("t_order")
@EruptDataProcessor(EruptJdbcDataService.DATA_PROCESSOR)
public class Order {

    @EruptField(views = @View(title = "ID"))
    private Long id;

    @EruptField(
        views = @View(title = "Number"),
        edit = @Edit(title = "Number", notNull = true, search = @Search)
    )
    private String number;

    @EruptField(
        views = @View(title = "Amount"),
        edit = @Edit(title = "Amount", type = EditType.NUMBER)
    )
    private BigDecimal amount;

    @EruptField(
        views = @View(title = "Placed"),
        edit = @Edit(title = "Placed", type = EditType.DATE_TIME)
    )
    private LocalDateTime placedAt;
}
```

### Secondary data source

Register any `DataSource` bean and reference it by name:

```java
@Configuration
public class ReadOnlyDataSourceConfig {
    @Bean("reporting")
    public DataSource reportingDataSource() { /* build HikariDataSource */ }
}

@EruptJdbc(value = "v_daily_sales", datasource = "reporting")
public class DailySales { ... }
```

## Operations

Full CRUD: list / find / add / edit / delete. Paging uses `LIMIT / OFFSET` — verify your database supports this syntax (MySQL, PostgreSQL, H2, SQLite, MariaDB do; Oracle and SQL Server need a Spring dialect adjustment).

## Gotchas

- **Column names = field names.** Rename with `@Column` on the field if needed (only if you use the JPA-style utility separately) or match your Java field name to the SQL column verbatim.
- Type conversions between JDBC `Number` / `Timestamp` / `Date` and Java types (`Integer`, `LocalDateTime`, `LocalDate`, `BigDecimal`, ...) are handled automatically.
