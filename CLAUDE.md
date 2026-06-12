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
mvn test -pl erupt-test -Dtest=EruptTest

# Run a specific test method
mvn test -pl erupt-test -Dtest=EruptTest#modules

# Run the sample application for local testing
cd erupt-sample && mvn spring-boot:run

# Release build (GPG signing, Javadoc, publishes to Maven Central)
mvn clean package -P release

# Update all module versions
mvn versions:set -DnewVersion=x.x.x
```

Java 17 required. Spring Boot 3.x. No Maven wrapper — use system `mvn`.

## Architecture Overview

Erupt is a **low-code platform framework** that auto-generates admin UIs from Java annotations, with zero frontend code required.

### Module Structure

The project is a multi-module Maven project (~25 modules):

| Group | Modules |
|-------|---------|
| Core | `erupt-annotation` (annotation declarations), `erupt-core` (runtime engine), `erupt-toolkit` |
| Data | `erupt-data/erupt-jpa` (default), `erupt-data/erupt-mongodb` |
| Features | `erupt-upms` (user/permissions), `erupt-security`, `erupt-excel`, `erupt-web` (frontend assets) |
| Templates | `erupt-tpl/{ant-design,element-ui,element-plus,amis}` |
| AI | `erupt-ai` (LLM + MCP integration via langchain4j), `erupt-ai-claw` (turn-key admin agent) |
| Extra | `erupt-extra/{erupt-job,erupt-generator,erupt-designer,erupt-monitor,erupt-magic-api,erupt-websocket,erupt-notice,erupt-print,erupt-terminal}` |
| Cloud | `erupt-cloud/{erupt-cloud-server,erupt-cloud-node,erupt-cloud-node-jpa}` |
| Dev | `erupt-test` (JUnit 5 + H2), `erupt-sample` (runnable demo) |

### Core Patterns

**1. Annotation-Driven UI Generation**
`@Erupt` on a class + `@EruptField` on fields → auto-generates tables, forms, search, permissions. Sub-annotations `@View`, `@Edit`, `@Search`, `@EditType` control rendering behavior.

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
