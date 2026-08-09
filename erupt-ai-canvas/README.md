# erupt-ai-canvas

AI generated HTML view pages for the Erupt admin console.

Describe a page in natural language, pick the Erupt models it reads from, and the LLM writes a complete self-contained HTML page (Vue 3 + Element Plus) that fetches its data through the Erupt REST API. The page is stored in the database and served at a stable URL — no frontend build, no restart.

## How it works

1. **AI Canvas** menu → create a record: name, optional dedicated LLM.
2. Row operation **Designer** opens the conversational designer: pick a data model and style, describe the page, iterate over versions. Each round builds a prompt from:
   - a built-in API skill (`prompts/ai-canvas-skill.md`) teaching the model the `data/table` list API, `TableQuery`/`Page` shapes, token handling and bundled frontend assets;
   - the field structure (field name / title / edit type / java type) of the target model, resolved at runtime;
   - the user message — plus the current HTML when regenerating, so edits become revisions.
3. The returned ```html``` block is stored per version; the active version is what visitors see.
4. The **Path** column shows the access route of each page.

## Serving & permissions

Each view gets a short unique `code` on creation. Pages are accessed at the frontend route `#/ai/canvas/{code}`: the frontend fetches the page source from `GET /erupt-api/ai-canvas/html/{code}` (login required) and embeds it in an iframe. To expose a page to users, create a menu pointing at that route.

The generated page calls the data APIs with the visitor's own token, so row-level data permissions always follow the logged-in user: non-admin users still need menu access to the target models.

The literal `${base}` placeholder in stored HTML is replaced with the servlet context path at render time, keeping asset and API URLs correct under any deployment path.

## Dependency

```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-ai-canvas</artifactId>
    <version>${erupt.version}</version>
</dependency>
```

Requires at least one enabled LLM configured in the `erupt-ai` module (a default chat model, or one selected per view).
