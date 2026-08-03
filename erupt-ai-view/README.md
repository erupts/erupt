# erupt-ai-view

AI generated HTML view pages for the Erupt admin console.

Describe a page in natural language, pick the Erupt models it reads from, and the LLM writes a complete self-contained HTML page (Vue 3 + Element Plus) that fetches its data through the Erupt REST API. The page is stored in the database and served at a stable URL — no frontend build, no restart.

## How it works

1. **AI View** menu → create a record: name, target models, requirement (natural language), optional dedicated LLM.
2. Row operation **Generate** → the service builds a prompt from:
   - a built-in API skill (`prompts/ai-view-skill.md`) teaching the model the `data/table` list API, `TableQuery`/`Page` shapes, token handling and bundled frontend assets;
   - the field structure (field name / title / edit type / java type) of every target model, resolved from `EruptCoreService` at runtime;
   - the user requirement — plus the current HTML when regenerating, so edits become revisions.
3. The returned ```html``` block is stored in the `html` column (editable as a code editor field for manual tweaks).
4. Row operation **Preview** opens the page in a modal; the **Path** column shows the serving URL.

## Serving & permissions

Pages are served at `GET /erupt-api/ai-view/render/{id}` (login required, token via `_token` URL param). To expose a page to users, create a menu of type **Link** pointing at that path — the frontend appends `_token` automatically.

The generated page calls the data APIs with the visitor's own token, so row-level data permissions always follow the logged-in user: non-admin users still need menu access to the target models.

The literal `${base}` placeholder in stored HTML is replaced with the servlet context path at render time, keeping asset and API URLs correct under any deployment path.

## Dependency

```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-ai-view</artifactId>
    <version>${erupt.version}</version>
</dependency>
```

Requires at least one enabled LLM configured in the `erupt-ai` module (a default chat model, or one selected per view).
