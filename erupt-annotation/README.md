# erupt-annotation

Core annotation declarations for the Erupt framework.

Defines all annotations used to drive automatic UI and API generation:
- `@Erupt` — marks a class as a managed data model
- `@EruptField` — configures field rendering, editing, and search behavior
- Sub-annotations: `@View`, `@Edit`, `@Search`, `@Tpl`, `@Power`, `@Vis`, `@Layout`, `@Dynamic`, etc.

No runtime dependencies. This module is the contract that all other Erupt modules depend on.
