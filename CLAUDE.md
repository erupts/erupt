# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

# Publish to Maven Central (GPG signing, Javadoc; wraps `mvn clean deploy -P release`)
./scripts/deploy.sh

# Bump version across this repo AND satellite repos (erupt-web etc.)
./scripts/bump-erupt.sh [--dry-run] x.x.x
```

Java 17 required. Spring Boot 3.5.x (pinned via `spring.boot.version` in the parent pom). No Maven wrapper — use system `mvn`.

## Architecture Overview

Erupt is a **low-code platform framework** that auto-generates admin UIs from Java annotations, with zero frontend code required.

### Module Structure

The project is a multi-module Maven project (~50 modules):

| Group | Modules |
|-------|---------|
| Core | `erupt-annotation` (annotation declarations), `erupt-core` (runtime engine) |
| Data | `erupt-data/erupt-data-{jpa,mongodb,memory,http,jdbc,es,ldap,k8s,s3,redis,file,feishu,notion}` — JPA is the default; the rest are pluggable connectors |
| Platform | `erupt-upms` (user/permissions), `erupt-security`, `erupt-admin`, `erupt-web` (frontend assets) |
| Starter | `erupt-starter/{erupt-spring-boot-starter,erupt-spring-boot-starter-all}` |
| Templates | `erupt-tpl/erupt-tpl` (engine) + `erupt-tpl/{ant-design,element-ui,element-plus,amis}` (skins) |
| AI | `erupt-ai/{erupt-ai,erupt-ai-canvas,erupt-ai-claw,erupt-ai-staff}` (LLM + MCP via langchain4j) |
| Support | `erupt-support/{erupt-toolkit,erupt-excel,erupt-websocket}` (shared capabilities: cache/notify, import/export, realtime transport) |
| Plugin | `erupt-plugin/{erupt-report,erupt-designer,erupt-job,erupt-generator,erupt-monitor,erupt-magic-api,erupt-notice,erupt-print,erupt-terminal}` |
| Cloud | `erupt-cloud/{erupt-cloud-common,erupt-cloud-server,erupt-cloud-node,erupt-cloud-node-jpa}` |
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
