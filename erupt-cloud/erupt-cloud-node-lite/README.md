# erupt-cloud-node-lite

An erupt cloud node **without Spring**: serve `@Erupt` classes from plain Java, Kotlin, Solon, Quarkus or Vert.x
through an erupt server, which renders the admin UI, logs users in and checks menus. Java 17+. The runtime
dependencies are `erupt-annotation`, `erupt-cloud-common` and `linq.j`, all three dependency-free: models are
declared with the very annotations a Spring erupt application uses, so a class moves between the two unchanged.

| Module | Use it for |
|--------|------------|
| `erupt-cloud-node-lite` | plain Java / Kotlin on the JDK's HTTP server, or mounted in any HTTP stack |
| `erupt-cloud-node-solon` | Solon 4: a plugin serving your `Node` bean on Solon's server |
| `erupt-cloud-node-quarkus` | Quarkus 3: serves your `Node` bean on Quarkus' HTTP server |
| `erupt-cloud-node-vertx` | Vert.x 4.5 / 5: mounts a node on a Vert.x Web router |
| `erupt-cloud-node` / `-jpa` | Spring Boot: the full erupt-core pipeline, not this module |

It is released with erupt, so its version is erupt's and the annotations always match the server.

```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-cloud-node-lite</artifactId>   <!-- or -solon, -quarkus, -vertx -->
    <version>${erupt.version}</version>
</dependency>
```

```java
@Erupt(name = "Article", orderBy = "id desc")
public class Article {
    @EruptField
    private Long id;

    @EruptField(views = @View(title = "Title", sortable = true),
            edit = @Edit(title = "Title", notNull = true, search = @Search))
    private String title;

    @EruptField(views = @View(title = "Publish Date"), edit = @Edit(title = "Publish Date", search = @Search))
    private LocalDate publishDate;

    @EruptField(edit = @Edit(title = "Body", type = EditType.TEXTAREA))
    private String body;
}

new Node("blog", System.getenv("ERUPT_ACCESS_TOKEN"), "http://erupt-server:8080")
        .register(Article.class, new JdbcRepository<>(dataSource, "article"))
        .serve("0.0.0.0", 9090);
```

1. On the erupt server create node `blog` (Node Config) and copy its access token.
2. Add a menu of type `table` with value `blog.Article`.
3. Run the node. The server must reach it on `http://<node ip>:9090`; set `.address(...)` otherwise.

## Models

Exactly erupt's rules: the class needs a no-argument constructor; every `@EruptField` member is a column
named like the member; the primary key is `@Erupt(primaryKeyCol)` (`id`), declared with a bare `@EruptField`.
A field shows in the form when its `@Edit` has a title. `AUTO` types are inferred from the member type as the
erupt proxies do (`String` INPUT, numbers NUMBER, `Boolean` BOOLEAN, `LocalDate` / `LocalDateTime` / `Date` DATE,
an enum CHOICE, another `@Erupt` class a reference). The build JSON is serialized from the annotations by
erupt's own rules (`AnnotationProcess`): `@Transient` attributes left out, `@Match` branches evaluated, enums
by name, classes by simple name.

`@Erupt(orderBy)` is the default sort, written `"field [asc|desc], ..."`.

## References

A member typed with another `@Erupt` class (registered on the same node) references one of its records. The
repository stores the referenced primary key; rows carry it as the key and the server resolves the label.

```java
@EruptField(views = @View(title = "Category", column = "name"), edit = @Edit(title = "Category", search = @Search))
private Category category;                       // table picker

@EruptField(views = @View(title = "Topic", column = "name"), edit = @Edit(title = "Topic",
        type = EditType.REFERENCE_TREE, referenceTreeType = @ReferenceTreeType(pid = "parent")))
private Category topic;                          // tree picker over Category.parent
```

## Handlers

The erupt-annotation interfaces, named in the annotations, work as in a Spring application:

| Interface | Declared in | |
|-----------|-------------|-|
| `DataProxy<T>` | `@Erupt(dataProxy)` | `validate`, `before/afterAdd`, `before/afterUpdate`, `before/afterDelete`, `afterFetch`, `addBehavior`, `editBehavior`, `searchCondition` |
| `OperationHandler<T, Form>` | `@RowOperation(operationHandler)` | `exec(rows, form, params)`; a returned string runs as JavaScript in the browser, so return only code you wrote. `eruptFormValue` fills the form |
| `ChoiceFetchHandler` | `@ChoiceType(fetchHandler)` | dynamic CHOICE options |
| `PowerHandler` | `@Power(powerHandler)` | narrows the declared power |
| `ExprBool.ExprHandler` | `show` / `ifRender` | per-user visibility of operations, views, edits |
| `Readonly.ReadonlyHandler` | `@Readonly(exprHandler)` | |

Throw `xyz.erupt.annotation.exception.EruptException` to show the user a message. Inside a request,
`Node.currentUser()` is the acting user and `Node.current().repository(Article.class)` a registered repository.

```java
@Erupt(name = "Article", rowOperation = @RowOperation(code = "publish", title = "Publish",
        eruptClass = PublishForm.class, operationHandler = Publish.class,
        show = @ExprBool(exprHandler = EditorsOnly.class)))
public class Article { ... }

public class Publish implements OperationHandler<Article, PublishForm> {
    public String exec(List<Article> rows, PublishForm form, String[] params) {
        var repo = Node.current().repository(Article.class);
        rows.forEach(a -> { a.setStatus("published"); repo.update(a); });
        return null;
    }
}
```

Handlers are created by their no-argument constructor, once per class. With a DI container, hand the node
its lookup (a null answer falls back to the constructor): `node.factory(beanContainer::get)` (Micronaut `ctx::getBean`, Quarkus `c -> Arc.container().instance(c).get()`, Solon `Solon.context()::getBean`,
Guice `injector::getInstance`).

## Storage

Storage is any `Repository<T>` (`page`, `get`, `add`, `update`, `delete`) over instances of the model class;
`Model.get` / `Model.set` read and write a column's stored value. Shipped:

- `MemoryRepository` for tests and demos;
- `JdbcRepository(dataSource, table)` over any JDBC `DataSource`. Columns are the member names (quoted, so case
  sensitive on PostgreSQL and Oracle; a reference column holds the referenced key), every value is a bound
  parameter, LIKE escapes with `ESCAPE '!'`. Identifier quotes and paging are detected from the connection
  (`.dialect(Dialect.SQLSERVER)` to set them). Create the tables yourself.

Without a sort, every repository returns rows by primary key ascending.

## Frameworks

The adapters route only the paths the erupt server calls (`/erupt-api/**`, `/node-health`) next to the
application's own, connect once the HTTP server listens (on its port, unless `.address(...)` is set) and
deregister on shutdown. A context path or mount point is handled.

**Solon** (`erupt-cloud-node-solon`): declare the node as a bean; the plugin does the rest.

```java
@Configuration
public class EruptNodeConfig {
    @Bean
    public Node node(@Inject("${erupt.cloud-node.access-token}") String token, DataSource dataSource) {
        return new Node("blog", token, "http://erupt-server:8080")
                .factory(Solon.context()::getBean)
                .register(Article.class, new JdbcRepository<>(dataSource, "article"));
    }
}
```

**Quarkus** (`erupt-cloud-node-quarkus`, with `quarkus-vertx-http`, which any REST extension brings): produce the
node as a `@Singleton` (`Node` is final, so it cannot be an `@ApplicationScoped` proxy).

```java
public class EruptNodeConfig {
    @Produces
    @Singleton
    Node node(@ConfigProperty(name = "erupt.cloud-node.access-token") String token, AgroalDataSource dataSource) {
        return new Node("blog", token, "http://erupt-server:8080")
                .factory(type -> Arc.container().instance(type).get())
                .register(Article.class, new JdbcRepository<>(dataSource, "article"));
    }
}
```

**Vert.x** (`erupt-cloud-node-vertx`): handlers run on worker threads, since repositories block.

```java
EruptNodeVertx.serve(vertx, node, 9090);                               // a server of its own

EruptNodeVertx.mount(router, node);                                    // or on the application's router
vertx.createHttpServer().requestHandler(router).listen(8080).onSuccess(s -> node.connect(s.actualPort()));
```

**Anything else** (servlet, Ktor, Javalin, Micronaut...): `node.handle(Request)` answers one request and never
throws. Mount it, call `node.connect(port)` once listening and `node.close()` on shutdown.

```java
var target = Node.Request.target(req.getContextPath(), req.getRequestURI(), req.getQueryString());
var r = node.handle(new Node.Request(req.getMethod(), target, req::getHeader, req.getInputStream().readAllBytes()));
resp.setStatus(r.status()); resp.setContentType(r.contentType()); resp.getOutputStream().write(r.body());
```

`target` is the raw path and query below the mount point, as received: the server's signature covers it.

## Security

Server calls are verified by `Erupt-Signature` (`.requireSignature(true)` refuses the plain access token);
`Erupt-Enforced` is trusted only under a valid signature. Calls with an `Origin` header are refused. Without
`Erupt-Enforced: power` the node asks the server for the user's rights (`erupt-power`, cached 5 s) and fails
closed. Condition keys and sort fields are whitelisted against the model.

Not yet: sub-tables (TAB_*), MULTI_CHOICE / collection members, TPL operations, tree models, depend fields of
references, file upload, Excel, `@Erupt(filter)`, i18n of messages.

## Development

```bash
mvn test -pl erupt-cloud/erupt-cloud-node-lite                         # node, SQL, H2
ERUPT_TEST_DATABASES=1 mvn test -pl erupt-cloud/erupt-cloud-node-lite -Dtest=DatabaseTest   # PostgreSQL / MySQL (PG*, MYSQL_* variables)
mvn test -pl erupt-cloud/erupt-cloud-node-vertx -Dvertx.version=5.2.1  # the Vert.x 5 line
```

The cross-language conformance suite and the Kotlin blog example live in
[erupt-sdk](https://github.com/erupt-io/erupt-sdk) (`jvm/`), built against this module.
