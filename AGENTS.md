# AGENTS.md

This file provides guidance to coding agents (Claude Code, Codex, ...) when working with code in this repository.

## Build & Test Commands

```bash
# Build entire project
mvn clean install

# Build a single module (and its dependencies)
mvn clean install -pl erupt-core -am

# Skip tests for faster builds
mvn clean install -DskipTests

# Run all tests (erupt-test module uses H2 in-memory DB)
mvn test -pl erupt-test

# Run a specific test class
mvn test -pl erupt-test -Dtest=LambdaQueryTest

# Run a specific test method
mvn test -pl erupt-test -Dtest=LambdaQueryTest#lambdaQueryTest

# Run the sample application for local testing
cd erupt-sample && mvn spring-boot:run

# Regenerate the OpenAPI spec (openapi/erupt.json) from controller Javadoc via smart-doc; commit the result
mvn -pl erupt-test -Popenapi process-classes

# Regenerate the node SDK contract (schema/*.json, consumed by github.com/erupt-io/erupt-sdk) after an
# annotation change; a plain `mvn test` fails while these files are stale. Commit the result
mvn -pl erupt-test -Pnode-schema test

# Publish to Maven Central (GPG signing, Javadoc; wraps `mvn clean deploy -P release`)
./scripts/deploy.sh

# Bump version across this repo AND satellite repos (erupt-web etc.)
./scripts/bump-erupt.sh [--dry-run] x.x.x

# Full release SOP (frontend build, bump, Maven Central, Docker Hub, merge+tag, gitee sync)
# Driven phase by phase by the /release command (.claude/commands/release.md)
./scripts/release.sh <preflight|frontend|bump|maven|docker|publish|start|verify> [x.x.x]
```

Java 17 required. Spring Boot 3.5.x (pinned via `spring.boot.version` in the parent pom). No Maven wrapper — use system `mvn`.

## Compatibility

Erupt runs on **Java 17 or newer** and on **every mainstream database**. Both are promises to users,
not defaults of the machine a feature happens to be written on — check a new feature against them.

- **Java 17 is the floor.** The parent pom compiles with `<release>17</release>`, so a newer API fails
  the build rather than the user's runtime. Developing on a newer JDK is fine; `List.getFirst()`,
  `SequencedCollection` and friends are not.
- **The dialect owns the SQL.** Never write a type name, a function or a DDL fragment that only one
  database understands. Ask Hibernate for it (`DdlTypeRegistry`, `Dialect`) or keep it in JPQL /
  criteria. A `columnDefinition` is raw DDL and travels to every database as written.
- **Watch what schema generation freezes.** `ddl-auto=update` adds columns and nothing else — it never
  changes a type, a check constraint or an enum value list. Anything the create statement pins down
  becomes permanent for databases already in the field (see `EnumColumnIntegrator`).
- **Mind reserved words and identifier case.** Column names such as `value` or `order` need quoting;
  erupt enables `globally_quoted_identifiers` for that reason, so identifiers are case sensitive.
- **Test against more than H2.** `erupt-test` runs on H2 in MySQL mode; a feature that touches SQL,
  metadata or DDL deserves a look at how Oracle, PostgreSQL and SQL Server would read it.

### Database compatibility review

A change that touches an entity mapping, generated DDL, a native query, a Hibernate hook (`Integrator`,
`Dialect`, type contributions) or anything in `erupt-data-jpa` / `erupt-data-jdbc` is reviewed against
PostgreSQL, MySQL and H2 before it is merged, and against Oracle and SQL Server by reading the dialect
when it cannot be run. State the result in the PR description, as the extension section does. Go through:

- **Types.** A varchar is not a number: MySQL and H2 coerce `'1' = 1`, PostgreSQL rejects it. Keep enums
  stored as ordinals in integer columns, booleans as booleans, and never compare across types in a check.
- **Check constraints and native enums.** H2 and MySQL turn `@Enumerated(STRING)` into a native enum
  type and write no check, so a passing H2 test proves nothing about the `check (... in (...))`
  PostgreSQL, Oracle and SQL Server emit. Read the generated DDL on PostgreSQL, not only on H2. Entity
  enums are always `@Enumerated(EnumType.STRING)`: `EnumColumnIntegrator` relaxes those alone and
  warns about an ordinal at startup.
- **Identifiers.** Quoted identifiers are case sensitive everywhere but folded differently when
  unquoted (Oracle and H2 upper-case, PostgreSQL lower-cases, MySQL keeps them). A filter, a native
  query or an `information_schema` lookup must agree with the DDL on quoting.
- **Dialect-only syntax.** `limit`, `ifnull`, `now()`, backticks, `auto_increment`, `text` and
  `datetime` are not portable. Prefer JPQL or Hibernate functions; a native fragment needs a reason and
  a note on each database it was checked on.
- **Schema update is append-only.** Whatever the create statement pins (type, check, default, enum
  list) stays forever on databases already created; a fix to generation must also cope with schemas
  the old generation left behind.

To run the suite on a local PostgreSQL, add `org.postgresql:postgresql` with test scope to
`erupt-test/pom.xml` for the run (do not commit it) and override the datasource on the forked JVM:

```bash
mvn -pl erupt-test -am -Dtest=EnumColumnTest -Dsurefire.failIfNoSpecifiedTests=false \
  -DargLine="-Dspring.datasource.url=jdbc:postgresql://localhost:5432/erupt_test \
  -Dspring.datasource.username=<user> -Dspring.datasource.password= \
  -Dspring.datasource.driver-class-name=org.postgresql.Driver -Dspring.jpa.database=postgresql" test
```

`-am` matters: without it `erupt-test` resolves the sibling modules from `~/.m2` and tests yesterday's
jar. The same override with a MySQL URL and driver checks MySQL.

## Extension Compatibility

A feature that works in the erupt core is not done until it also holds for the four extensions built on
top of it. Every new feature or behavior change must be checked against each of them; state the result
(affected or not, and what was done) in the PR description.

- **erupt-cloud (remote models).** A model may live on a node, not in this JVM: the server then has no
  entity class, no JPA mapping, no `DataProxy` and no Spring bean for it. A feature that reaches for
  `EruptModel.getClazz()`, `eruptDao` on the entity, reflection on fields or a local handler must either
  go through the wire protocol (`erupt-cloud/PROTOCOL.md`) or be explicitly switched off for cloud models
  — never fail silently. `erupt-cloud-node-lite` has no Spring at all, so node-side code cannot assume it.
- **erupt-sdk (non-Java nodes, `../erupt-sdk`).** Ten language SDKs implement the node side from
  `schema/` and `PROTOCOL.md` alone. A new annotation attribute, field type, endpoint or response field
  is a protocol change: regenerate `schema/` (`-Pnode-schema`), update `PROTOCOL.md`, keep the change
  backward compatible (old nodes omit the new field — the server must default it), and note whether the
  SDKs and the conformance suite need a follow-up.
- **erupt-tenant (multi-tenant SaaS, `../erupt-tenant`).** It builds on `MetaUser.tenantId`,
  `UpmsTenantSessions`, `UpmsTenant`, `EruptUser`, `EruptMenu`, `LoginModel`, the token/session services
  and interceptor ordering. New tables holding per-user or business data, caches, Redis keys, broadcasts,
  scheduled jobs and file paths must be tenant-scoped (one tenant never sees another's data); new menus
  must pass through the tenant menu scope; and a signature change to an upms/core class it imports
  breaks it — grep `../erupt-tenant` before renaming or changing one.
- **erupt-designer (runtime-designed models, `erupt-plugin/erupt-designer`).** A model drawn in the
  designer is published without code: its class is generated at runtime (`DesignerClassFactory`, a fresh
  classloader per publish) and carries fields only — the annotations exist as JSON disguised through
  `JsonAnnotationProxy` over `EruptDesignerTemplate`, and the rows live in an embedded SQLite store
  (`DesignerDataService`), not JPA. A feature must read annotations through `EruptModel` /
  `EruptFieldModel` (never `getClazz().getAnnotation(...)` or field reflection), must not assume an entity,
  `eruptDao`, JPQL or the main database, and must not cache by `Class` across publishes; otherwise switch
  it off explicitly via `DesignerClassFactory.designerClass`. A new annotation attribute falls back to the
  template default until the designer exposes it, an annotation array member needs a prototype element in
  the template, and a new `EditType` needs a template field (`EruptDesignerService.templateField`) — note
  whether the designer UI needs a follow-up.

## Architecture Overview

Erupt is a **low-code platform framework** that auto-generates admin UIs from Java annotations, with zero frontend code required.

### Module Structure

The project is a multi-module Maven project (~50 modules):

| Group | Modules |
|-------|---------|
| Core | `erupt-annotation` (annotation declarations), `erupt-core` (runtime engine) |
| Data | `erupt-data/erupt-data-{jpa,mongodb,memory,http,jdbc,es,ldap,k8s,s3,redis,file,feishu,notion,dingtalk,airtable}` — JPA is the default; the rest are pluggable connectors |
| Platform | `erupt-upms` (user/permissions), `erupt-security`, `erupt-admin`, `erupt-web` (frontend assets) |
| Starter | `erupt-starter/{erupt-spring-boot-starter,erupt-spring-boot-starter-all}` |
| Templates | `erupt-tpl/erupt-tpl` (engine) + `erupt-tpl/{ant-design,element-ui,element-plus,amis}` (skins) |
| AI | `erupt-ai/{erupt-ai,erupt-ai-canvas,erupt-ai-claw,erupt-ai-staff,erupt-ai-rag,erupt-ai-dev}` (LLM + MCP via langchain4j; RAG knowledge base with Qdrant/Milvus; erupt-ai-dev is AI software delivery: projects with git repos, a requirement pool scoped by `@EruptScope`, worktrees read live from git) |
| Support | `erupt-support/{erupt-toolkit,erupt-excel,erupt-websocket,erupt-git}` (shared capabilities: cache/notify, import/export, realtime transport, git over the CLI: bare repos, worktrees, rebase/push) |
| Plugin | `erupt-plugin/{erupt-report,erupt-designer,erupt-job,erupt-generator,erupt-monitor,erupt-magic-api,erupt-notice,erupt-print,erupt-terminal,erupt-remote,erupt-comment,erupt-revision,erupt-file-center}` — erupt-file-center is a Finder-style file center over the upload directory, extra folders and the S3 bucket — the ready-made Kubernetes admin built on erupt-data-k8s lives in its own repo, github.com/erupt-io/erupt-k8s-dashboard |
| Cloud | `erupt-cloud/{erupt-cloud-common,erupt-cloud-server,erupt-cloud-node,erupt-cloud-node-jpa}`; `erupt-cloud-node-lite` is a node without Spring (only `erupt-annotation`, `erupt-cloud-common` and `linq.j`; common's `spring-web` is optional for that reason), mounted by `erupt-cloud-node-{solon,quarkus,micronaut,vertx,ktor,servlet}` (`-ktor` is Kotlin, built by `kotlin-maven-plugin`) |
| Deploy | `erupt-deploy/erupt-docker` (all-in-one Docker image; has its own `deploy.sh`) |
| Dev | `erupt-test` (JUnit 5 + H2), `erupt-sample` (runnable demo) |

`erupt-plugin/erupt-template` is a scaffold for new plugin modules — it exists on disk but is intentionally excluded from the parent pom's `<modules>`.

### Core Patterns

**1. Annotation-Driven UI Generation**
`@Erupt` on a class + `@EruptField` on fields → auto-generates tables, forms, search, permissions. Sub-annotations `@View`, `@Edit`, `@Search`, `@EditType` control rendering behavior. `@EruptCube` (in erupt-annotation's `cube` package, with `@Dimension`/`@Measure`/`@Explore`) declares OLAP-style analytics views on entities — see `EruptOperateLog` for an example.

**2. Dynamic Proxy Layer**
`AnnotationProxy<T,R>` converts annotations to JSON for the frontend using Spring AOP. Key classes: `EruptProxy`, `EruptFieldProxy`, `AnnotationProxyPool`. Located in `erupt-core/.../proxy/`.

**3. Pluggable Module System**
Every module implements `EruptModule` (defines `info()`, `run()`, `initMenus()`, `initFun()`). Modules self-register via Spring Boot auto-configuration (`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`). `EruptModuleInvoke` orchestrates startup.

**4. Thread-Local Request Context**
`MetaContext` (InheritableThreadLocal) carries per-request user info, token, and erupt entity metadata through the call stack. Located in `erupt-core/.../context/MetaContext.java`.

**5. SpEL Expression Support**
`@Match` and similar annotations accept Spring Expression Language strings for runtime conditional rendering, visibility, and row-level permission filtering. `ExprInvoke` handles evaluation.

### Data Flow

Request → Spring MVC Controller → `MetaContext` populated → `EruptCoreService` resolves `@Erupt` model → Annotation proxy converts to JSON → Data layer (`DataProcessorManager` → JPA/Mongo) → JSON response to Angular frontend.

### DataProxy Lifecycle Hooks

Implement `DataProxy<T>` and register via `@Erupt(dataProxy = MyProxy.class)` to hook into:
- `beforeAdd` / `afterAdd`, `beforeUpdate` / `afterUpdate`, `beforeDelete` / `afterDelete`
- `beforeFetch` / `afterFetch`, `searchCondition`, `validate`
- Excel import/export, print, and UI behavior hooks

### Handler Extension Points

Implement these interfaces and reference them in annotations for custom behavior:
- `OperationHandler` — custom row-level operations
- `ChoiceFetchHandler` / `TagsFetchHandler` — dynamic dropdown/tag data
- `FilterHandler` — custom query filters
- `PowerHandler` — custom permission logic
- `AutoCompleteHandler` — search auto-complete

### LambdaQuery (Type-Safe Queries)

Use `eruptDao.lambdaQuery(Entity.class)` for type-safe JPA queries using method references instead of strings:
```java
eruptDao.lambdaQuery(EruptUser.class).like(EruptUser::getName, "e").list()
```

### erupt-ai Module (Active Development)

Uses **langchain4j 1.14.1** and **langchain4j-mcp/langchain4j-agentic-a2a 1.14.1-beta24**. Key classes:
- `LlmCore` — abstract base; maintains static LLM registry; wires chat memory, tools, SSE streaming
- `LLMService` — manages provider configs and caches `ChatModel` instances with hot-swap support
- `AiToolboxManager` — Spring beans annotated with `@AiToolbox` + LangChain4j `@Tool` are auto-exposed as AI-callable tools
- `McpServerService` — connects/health-checks MCP servers over SSE and STDIO transports
- `A2AAgentService` — agent-to-agent (A2A) protocol for multi-agent workflows
- `LLMRoleService` — role-based access control for which tools each user can invoke
- `ChatController` — `/ai/chat` streaming and non-streaming endpoints
- 17 built-in LLM adapters in `llm/` package (ChatGPT, Claude, DeepSeek, Gemini, Ollama, Qwen, GLM, etc.)

### Testing

Tests live in `erupt-test/`. Base class `EruptApplicationTests` provides a login helper. Uses H2 in-memory DB configured in `erupt-test/src/test/resources/application.yml`. Test data generation via **Instancio**.

**One Spring context per JVM.** erupt keeps static registries (`EruptSpringUtil`'s context, the erupt models,
the AI tool beans), so a second context started in the same JVM repoints them at its own beans and later
tests act on the wrong context. Never give a test `@Import`, `@TestConfiguration`, `@MockBean`,
`@TestPropertySource` or `@SpringBootTest(classes/properties)` of its own: put a stand-in bean into the
shared context (e.g. `StandInTenantModule`) instead. A test that truly must boot its own application is
tagged `@Tag("own-context")`, which surefire runs in a separate JVM (see `erupt-test/pom.xml`).

### Caveats

- **`spring-boot-devtools` is incompatible** — the framework detects and warns at startup.
- JSON serialization uses **GSON** (not Jackson) throughout the framework.

## Code Style

- **All code comments must be in English.** This includes inline comments, Javadoc, and section headers. Do not write Chinese comments even when the surrounding code or identifiers are Chinese.

## Internationalization (i18n)

- **Default language is English.** All user-visible strings — `@Erupt(name=...)`, `@EruptField` titles, menu names, error messages — must be written in English.
- **Every module must ship a CSV.** Place translations at `src/main/resources/i18n/<module-name>.i18n.csv`. `I18nRunner` auto-discovers all `i18n/*.csv` files on the classpath; no registration needed.
- **CSV key = English text.** The key doubles as the en-US fallback, so `I18nTranslate.$translate(key)` degrades gracefully when no translation is found.
- **Key case is significant.** The inner lookup map is a plain `HashMap` — `"Name"` and `"name"` are different keys. Match the exact string used in the annotation or code.
- **Wrap runtime error messages.** Any `EruptWebApiRuntimeException` (or similar) message shown to the frontend must go through `I18nTranslate.$translate("module.key")`. Static/internal developer errors do not need translation.
- **No Chinese hard-coded anywhere.** Java source files must contain zero CJK characters — including test assertion literals. Use `assertNotEquals` / language-neutral checks instead of hard-coded translated values.
