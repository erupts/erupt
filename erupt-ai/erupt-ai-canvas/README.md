# erupt-ai-canvas

AI generated HTML view pages for the Erupt admin console.

Describe a page in natural language, pick the Erupt models it works on, and the LLM writes a complete self-contained HTML page (Vue 3 + Element Plus) that reads — and, per model, optionally creates, updates or deletes — its data through the Erupt REST API. The page is stored in the database and served at a stable URL — no frontend build, no restart.

## How it works

1. **AI Canvas** menu → create a record: name and the data models the page works on — one MULTI_FORM block per model (data source type, model, optional purpose hint, and the **Allow Add / Allow Edit / Allow Delete** switches). Bindings can be edited later, e.g. to pull in a related model or to open up a write.
2. Row operation **Designer** opens the conversational designer: pick a style, describe the page, iterate over versions. Each round builds a prompt from:
   - a built-in API skill (`prompts/ai-canvas-skill.md`) teaching the model the `data/table` list API, `TableQuery`/`Page` shapes, token handling and bundled frontend assets;
   - one data-access guide per bound data source type, plus its write guide (SDK write calls, payload shapes per edit type, form rules) when a binding of that type allows a write;
   - the field structure (field name / title / edit type / java type) of every bound model plus its purpose hint and allowed writes, resolved at runtime;
   - the user message — plus the current HTML when regenerating, so edits become revisions.
3. The returned ```html``` block is stored per version; the active version is what visitors see.
4. The **Path** column shows the access route of each page.

## Writes

Bindings are read-only by default. Switching on **Allow Add / Allow Edit / Allow Delete** for a model lets the generated page call `Erupt.add` / `Erupt.update` / `Erupt.remove` on it; the prompt then also teaches the form option lookups (`Erupt.initValue`, `Erupt.referenceTable`, `Erupt.referenceTree`, `Erupt.checkbox`, `Erupt.choice`) and the payload shape of every edit type. During generation the LLM dry-runs each planned write through a verification tool (permission check + the server's own validation, nothing persisted) before embedding it.

The switches only scope what the page offers. Every write is still executed with the visitor's token and re-checked against the model's power config and the visitor's menu / role permissions.

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
