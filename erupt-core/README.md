# erupt-core

The runtime engine of the Erupt framework.

Reads `@Erupt` and `@EruptField` annotations at startup, builds metadata models, and auto-generates CRUD REST APIs and UI descriptors — zero frontend code required.

Key components:
- `AnnotationProxy` — converts annotations to JSON for the frontend
- `MetaContext` — thread-local per-request context (user, token, entity)
- `DataProxy` — lifecycle hooks: `beforeAdd`, `afterUpdate`, `searchCondition`, etc.
- `EruptModuleInvoke` — orchestrates module startup and menu initialization
