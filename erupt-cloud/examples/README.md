# Example nodes

Runnable erupt cloud nodes, built with the erupt they ship with and never published. They are not in the default
build: the `node-examples` profile adds them.

| Module | What |
|--------|------|
| [`conformance`](conformance) | the fixture of [erupt-sdk](https://github.com/erupt-io/erupt-sdk)'s `conformance/run.py`, on erupt-cloud-node-lite (protocol v2) |
| [`showcase`](showcase) | erupt-sdk's [SHOWCASE.md](https://github.com/erupt-io/erupt-sdk/blob/develop/SHOWCASE.md) over SQLite on erupt-cloud-node-lite: every capability of the node, the same screens as each SDK's showcase |
| `showcase-solon`, `-quarkus`, `-micronaut`, `-vertx`, `-ktor`, `-servlet` | that same showcase node served by each lite adapter (`-servlet` in an embedded Tomcat) |
| [`blog`](blog) | a small blog in Kotlin over H2, on erupt-cloud-node-lite |
| [`spring-blog`](spring-blog) | the blog on Spring Boot with erupt-cloud-node-jpa (protocol v1) |

```bash
# every example, with the erupt modules it uses (from the erupt root)
mvn -P node-examples package -DskipTests -pl "$(ls -d erupt-cloud/examples/*/ | sed 's#/$##' | paste -sd, -)" -am

# the cross-language checks, from an erupt-sdk checkout (CI: .github/workflows/node-examples.yml); copy erupt's
# schema/ into it first when the annotations changed
E=../erupt/erupt-cloud/examples
python3 conformance/run.py -- java -jar $E/conformance/target/erupt-conformance.jar
python3 conformance/showcase.py -- java -jar $E/showcase/target/erupt-showcase.jar
python3 conformance/showcase.py -- java -jar $E/showcase-solon/target/erupt-showcase-solon.jar   # -micronaut, -vertx, -ktor, -servlet alike
python3 conformance/showcase.py -- java -jar $E/showcase-quarkus/target/quarkus-app/quarkus-run.jar
```

## Running one against a server

Create the node on the erupt server (Micronode management → Node configuration) and copy its access token, then:

```bash
ERUPT_NODE_NAME=showcase ERUPT_ACCESS_TOKEN=... ERUPT_SERVER=http://localhost:8080 PORT=9090 \
  java -jar erupt-cloud/examples/showcase/target/erupt-showcase.jar   # ERUPT_DB: the SQLite file, showcase.db by default
ERUPT_ACCESS_TOKEN=... ERUPT_SERVER=http://localhost:8080 PORT=9090 java -jar erupt-cloud/examples/blog/target/erupt-blog.jar
ERUPT_ACCESS_TOKEN=... ERUPT_SERVER=http://localhost:8080 PORT=9090 \
  java -jar erupt-cloud/examples/spring-blog/target/erupt-cloud-example-spring-blog.jar
```

On the server add menus of type `table` with the values `<node>.<Model>`: `showcase.Article`, `showcase.Category`,
`showcase.Author`, `showcase.Tag`, `showcase.Component`, `showcase.Playground`, `showcase.AuditLog`; `blog.Article` and
`blog.Category` for the blogs.

## Differences worth knowing

- Field naming follows Java (`publishDate`); the showcase names its members like the storage columns of SHOWCASE.md
  (`publish_date`), as every SDK's showcase does.
- The Spring node (`erupt-cloud-node`, `-jpa`) runs the full erupt-core pipeline and speaks protocol v1; the lite node
  speaks v2 like every erupt-sdk node, and leaves rights, query and record checks to the server.
- A wrong access token or a denied right answers **500** from the Spring node and **403** from the lite node and the SDKs
  (`erupt-cloud/PROTOCOL.md` §8 proposes 403 for v1). The browser shows an error either way.
