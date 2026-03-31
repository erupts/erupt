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

Java 17 required. No Maven wrapper — use system `mvn`.

## Architecture Overview

Erupt is a **low-code platform framework** that auto-generates admin UIs from Java annotations, with zero frontend code required.

### Module Structure

The project is a multi-module Maven project (~25 modules):

| Group | Modules |
|-------|---------|
| Core | `erupt-annotation` (annotation declarations), `erupt-core` (runtime engine), `erupt-toolkit` |
| Data | `erupt-data/erupt-jpa` (default), `erupt-data/erupt-mongodb`|
| Features | `erupt-upms` (user/permissions), `erupt-security`, `erupt-excel`, `erupt-web` (frontend assets) |
| Templates | `erupt-tpl/{ant-design,element-ui,element-plus,amis}` |
| AI | `erupt-ai` (LLM + MCP integration via langchain4j) |
| Extra | `erupt-extra/{erupt-job,erupt-generator,erupt-monitor,erupt-magic-api}` |
| Cloud | `erupt-cloud/{erupt-cloud-server,erupt-cloud-node,erupt-cloud-node-jpa}` |
| Dev | `erupt-test` (JUnit 5 + H2), `erupt-sample` (runnable demo) |

### Core Patterns

**1. Annotation-Driven UI Generation**
`@Erupt` on a class + `@EruptField` on fields → auto-generates tables, forms, search, permissions. Sub-annotations `@View`, `@Edit`, `@Search`, `@EditType` control rendering behavior.

**2. Dynamic Proxy Layer**
`AnnotationProxy<T,R>` converts annotations to JSON for the frontend. Key classes: `EruptProxy`, `EruptFieldProxy`, `AnnotationProxyPool`. Located in `erupt-core/src/main/java/xyz/erupt/core/proxy/`.

**3. Pluggable Module System**
Every module implements `EruptModule` (defines `info()`, `run()`, `initMenus()`, `initFun()`). Modules self-register via Spring Boot auto-configuration (`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`). `EruptModuleInvoke` orchestrates startup.

**4. Thread-Local Request Context**
`MetaContext` (InheritableThreadLocal) carries per-request user info, token, and erupt entity metadata through the call stack. Located in `erupt-core/.../context/MetaContext.java`.

**5. SpEL Expression Support**
`@Match` and similar annotations accept Spring Expression Language strings for runtime conditional rendering, visibility, and row-level permission filtering.

### Data Flow

Request → Spring MVC Controller → `MetaContext` populated → Annotation proxy resolves `@Erupt` model → Data layer (JPA/Mongo/ES) → JSON response to Angular frontend.

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

Uses **langchain4j** (1.11.0) and **langchain4j-mcp** (1.12.2-beta22). Key classes:
- `LlmCore`: Core LLM interaction
- `McpServer` / `McpServerService`: MCP protocol integration
- `EruptAiAutoConfiguration`: Module registration

### Testing

Tests live in `erupt-test/`. Base class `EruptApplicationTests` provides a login helper. Uses H2 in-memory DB configured in `erupt-test/src/test/resources/application.yml`. Test data generation via **Instancio**.

### Caveats

- **`spring-boot-devtools` is incompatible** — the framework detects and warns at startup.
- JSON serialization uses **GSON** (not Jackson) throughout the framework.
