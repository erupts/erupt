# Erupt Cloud Node Protocol — Draft v0

> Status: **draft**. Describes the wire contract between an erupt cloud **server** and a **node** as
> implemented today by `erupt-cloud-server` / `erupt-cloud-node`, so that a node can be written in any
> language. Sections marked **[v1 proposal]** are not implemented yet.

## 1. Roles

| Role | Responsibility |
|------|----------------|
| **Server** | Hosts the UI, login, users, menus and permissions (`erupt-upms`). Routes every request for a node-owned erupt to that node. |
| **Node** | Owns a set of erupt models and their data. Describes each model (build JSON) and serves its CRUD. Holds no users, no sessions, no menus. |

A browser never talks to a node. Every node request is a server-to-server forward.

```
Browser ──► Server /erupt-api/... (erupt = "nodeName.Article")
              │ login + menu check, strip "nodeName."
              ▼
            Node   /erupt-api/... (erupt = "Article")  ──► database / any backend
```

## 2. Transport

- HTTP/1.1, JSON bodies, UTF-8. The server connects with a 5s connect timeout.
- Node base URL = a `nodeAddress` it reports at registration (scheme + host + port + context path, no trailing slash).
- A node may run several instances; each instance registers its own address under the same `nodeName`.

## 3. Lifecycle

### 3.1 Prerequisite

An administrator creates the node on the server (`CloudNode` table) and gets its `nodeName` and
`accessToken`. A node whose record is disabled is refused at registration.

### 3.2 Health — node serves

```
GET {nodeAddress}/node-health
200  body: <nodeName>      (plain text)
```

The server treats a different non-blank body as a mismatch and the instance as down. Must answer within 1s.

### 3.3 Register / heartbeat — node calls

```
POST {server}/erupt-cloud-api/register-node
Content-Type: application/json
```

```json
{
  "instanceId": "aZxQwe",
  "nodeName": "blog",
  "accessToken": "<access token>",
  "version": "1.13.0",
  "nodeAddress": ["http://10.0.0.12:8080"],
  "eruptModules": [],
  "erupts": ["Article", "Category"],
  "resources": {}
}
```

| Field | Meaning |
|-------|---------|
| `instanceId` | Random id per process start. |
| `version` | Erupt version the node speaks. Today this doubles as the protocol version (see §9). |
| `nodeAddress` | Base URLs the server should call. Must be reachable from the server. |
| `erupts` | Node-local erupt names. Unique within the node, no `.` allowed. |
| `eruptModules` / `resources` | Informational, shown on the server's node page. May be empty. |

Rules:

- The node must already be serving `/node-health` before it registers: the server probes every
  `nodeAddress` synchronously and rejects the call if any probe fails.
- `2xx` = accepted. Anything else = rejected; retry with backoff.
- Repeat every **15s** (heartbeat). The server forgets a node after **10 min** without a heartbeat and
  also polls `/node-health` every **120s**, evicting dead instances.
- With several server addresses, round-robin between them.

### 3.4 Deregister — node calls on graceful shutdown

```
POST {server}/erupt-cloud-api/remove-instance-node
Content-Type: application/x-www-form-urlencoded

nodeName=blog&accessToken=<access token>&locations=http://10.0.0.12:8080
```

After it, the node should drain in-flight requests (Java default: up to 30s) before exiting.

## 4. Inbound requests (server → node)

### 4.1 Headers

| Header | Value | Node must |
|--------|-------|-----------|
| `Access-Token` | The node's access token | Compare (constant time) with its own; mismatch → reject. |
| `erupt` | Node-local erupt name, e.g. `Article` | 404 if the node does not own it. |
| `eruptParent` (or `_eruptParent` query param) | `nodeName.Parent`, present when a nested erupt (sub-table, tab, reference) is opened from a parent's menu | Strip the prefix; 404 unless `Parent` equals `erupt` or actually nests it. |
| `user` | Base64 of the JSON user, see below | Treat as the acting user. |
| `token` | The browser's session token | Forward it when calling back into the server (§6). Never validate or store it. |
| `Lang` | UI language, e.g. `en-US` | Optional: localize messages. |
| `Origin` | — | If **present**, reject with 403: a browser is calling directly. |
| `Drill`, `DrillValue`, `DrillSourceErupt` | Drill-down context (source erupt already un-prefixed) | Only for nodes that declare drills. |

`user` decodes to:

```json
{ "uid": 1, "account": "admin", "name": "Admin", "tenantId": null }
```

### 4.2 What the server has already checked

The server has verified the login and that the user holds the **menu** for the erupt (or for its
parent). It has **not** checked add / edit / delete / export rights. Those are the node's job (§6.1).

### 4.3 Naming

Paths, the `erupt` header and every name inside responses use the node-local name (`Article`).
On build responses the server rewrites names to `nodeName.Article` itself (`eruptName`, drill
`linkErupt`, `tabErupts`, `combineErupts`, `operationErupts`). A node must **not** add the prefix.

## 5. Endpoints a node serves

All paths are relative to the node base URL. `{erupt}` is the node-local name.

### 5.1 Level 0 — required

A node that implements only these gets a working table, search, detail view and add / edit / delete form.

| Method | Path | Body | Response |
|--------|------|------|----------|
| GET  | `/erupt-api/build/{erupt}` | — | `EruptBuildModel` |
| POST | `/erupt-api/data/table/{erupt}` | `TableQuery` | `Page` |
| GET  | `/erupt-api/data/{erupt}/{id}` | — | row object |
| GET  | `/erupt-api/data/init-value/{erupt}` | — | row object (form defaults, `{}` allowed) |
| POST | `/erupt-api/data/modify/{erupt}` | row object | `R` |
| POST | `/erupt-api/data/modify/{erupt}/update` | row object (with primary key) | `R` |
| POST | `/erupt-api/data/modify/{erupt}/delete` | `[id, ...]` | `R` |

### 5.2 Level 1 — needed only by the features a model declares

| Feature declared in build JSON | Endpoints |
|--------------------------------|-----------|
| `tree` | `GET /erupt-api/data/tree/{erupt}` |
| `linkTree` | `GET /erupt-api/data/depend-tree/{erupt}` |
| `rowOperation` | `POST /erupt-api/data/{erupt}/operator/{code}`, `POST .../operator/{code}/form-value` |
| `REFERENCE_TABLE` / `REFERENCE_TREE` | `POST /erupt-api/data/{erupt}/reference-table/{field}`, `GET /erupt-api/data/{erupt}/reference-tree/{field}` |
| `CHECKBOX` | `GET /erupt-api/data/{erupt}/checkbox/{field}` |
| `TAB_TREE` | `GET /erupt-api/data/tab/tree/{erupt}/{field}` |
| `TAB_TABLE_ADD` / tab editing | `POST /erupt-api/data/modify/tab-add|tab-update|tab-delete/{erupt}/{field}` |
| dynamic `CHOICE` / `TAGS` / `AUTO_COMPLETE` | `GET /erupt-api/comp/choice-item/{erupt}/{field}`, `POST /erupt-api/comp/choice-item-filter/{erupt}/{field}`, `POST /erupt-api/comp/tags-item/{erupt}/{field}`, `POST /erupt-api/comp/auto-complete/{erupt}/{field}` |
| `onchange` | `POST /erupt-api/data/onchange/{erupt}/{field}` |
| `BUTTON` | `POST /erupt-api/comp/button/{erupt}/{field}` |
| `cellEdit` | `POST /erupt-api/data/modify/{erupt}/update-cell` |
| `dragSort` | `POST /erupt-api/data/modify/{erupt}/drag-sort` |
| view `statistic` | `POST /erupt-api/data/aggregate/{erupt}` |
| extra rows | `POST /erupt-api/data/extra-row/{erupt}` |
| form-view layout | `GET`/`POST /erupt-api/data/form-view/{erupt}` |
| per-field sub-model | `GET /erupt-api/build/{erupt}/{field}` |

### 5.3 Level 2 — optional extensions

| Feature | Endpoints |
|---------|-----------|
| Attachments | `POST /erupt-api/file/upload/{erupt}/{field}`, `/uploads/...`, `/upload-html-editor/...` (multipart, field `file`) |
| Excel import / export | `/erupt-api/excel/...` |
| Gantt / board / calendar views | `/erupt-api/data/modify/gantt/...`, `/board/...`, `/calendar/...` |
| Template pages | any `/erupt-api/tpl/...` path, routed by the `_node` query param, forwarded verbatim |

A node answers **404** for anything it does not implement. **[v1 proposal]** It also lists what it
implements in a `capabilities` field at registration, so the server can hide the matching UI instead
of letting it fail.

## 6. Calls a node makes back to the server

| Method | Path | Headers | Returns |
|--------|------|---------|---------|
| GET | `/erupt-cloud-api/erupt-power?nodeName=&eruptName=` | `token` | `PowerObject` for the acting user |
| GET | `/erupt-cloud-api/userinfo/{nodeName}` | `token`, `Access-Token` | full user info |
| GET | `/erupt-api/erupt-permission/{menuValue}` | `token` | `true` / `false` |
| GET | `/erupt-cloud-api/node-config/{nodeName}` | `Access-Token` | node config string set on the server |
| GET | `/erupt-cloud-api/node-group-config/{nodeName}` | `Access-Token` | node group config string |

### 6.1 Permissions are enforced on the node

For every request the node computes the effective power as **declared power AND server power**:

1. Start from the model's declared `power` (all `true` by default).
2. Fetch `erupt-power` for the acting user and turn off each of `add`, `edit`, `delete`, `viewDetails`,
   `export`, `importable` that the server reports `false`.
3. Put the result in `EruptBuildModel.power`.
4. **Reject** a modify call whose right is off: add → `add`, update / update-cell / drag-sort → `edit`
   (update-cell also `cellEdit`), delete → `delete`, get-by-id → `edit` or `viewDetails`, table → `query`.

Skipping step 4 lets any user who can see a menu write to the data. Caching the power per `token` for a
few seconds is allowed.

### 6.2 Row-level and tenant scope

`user.tenantId` and `user.uid` are the only identity a node gets. Row filters and tenant isolation
are applied by the node in its queries.

## 7. Data shapes

### 7.1 `EruptBuildModel`

```json
{
  "eruptModel": {
    "eruptName": "Article",
    "eruptJson": { "name": "Article", "primaryKeyCol": "id", "power": {...}, "tree": {...}, "rowOperation": [], "drills": [], ... },
    "eruptFieldModels": [
      {
        "fieldName": "title",
        "fieldReturnName": "String",
        "eruptFieldJson": {
          "views": [{ "title": "Title", "sortable": true, ... }],
          "edit":  { "title": "Title", "notNull": true, "type": "INPUT", "search": { "value": true, ... }, "inputType": {...}, ... },
          "sort": 1000,
          "params": []
        },
        "componentValue": null
      }
    ],
    "searchCondition": {},
    "extraRow": false,
    "tags": {}
  },
  "power": { "add": true, "edit": true, "delete": true, "query": true, "viewDetails": true, "export": true, "importable": true, ... },
  "tabErupts": {},
  "combineErupts": {},
  "operationErupts": {}
}
```

- `eruptJson` is `@Erupt` serialized key by key: one key per annotation attribute, nested annotations as
  objects, enums as their names. `eruptFieldJson` is `@EruptField` the same way (`views`, `edit`, `sort`, `params`).
- The JSON is **fully resolved**. Expressions (`ExprBool`, `Expr`) are already evaluated and dynamic
  choices are already in `componentValue`, so the browser never runs node code.
- `fieldReturnName` is `"number"` for numeric fields, otherwise the type's simple name (element type for collections).
- `EditType.AUTO` must not appear. The node resolves it to a concrete type.

Two generated files in `schema/` are the source for SDK types and templates; both are checked for drift
by erupt-test, so an annotation change fails the build until they are regenerated:

| File | Generated by | Content |
|------|--------------|---------|
| `erupt-annotations.schema.json` | `NodeSchemaTest` | JSON Schema (2020-12) of `eruptJson`, `eruptFieldJson` and every annotation / enum they reach. `@Transient` attributes are absent, `@EruptProperty` aliases applied, `@Match` attributes marked `x-erupt-match`, `@ToMap` arrays become objects (`x-erupt-map-key`). |
| `erupt-node-defaults.json` | `NodeDefaultsTest` | Fully resolved field JSON per supported edit type, the default `eruptJson`, `power` and row operation. |

An `eruptJson` / `eruptFieldJson` a node emits that does not validate against the schema is a node bug.
`@Transient` attributes such as `Erupt.name` and `Erupt.orderBy` never reach the browser; a node applies
their meaning itself (e.g. the default sort).

### 7.2 `TableQuery` (request) / `Page` (response)

```json
{ "pageIndex": 1, "pageSize": 10,
  "sort": [{ "field": "createTime", "direction": "DESC" }],
  "condition": [{ "key": "title", "expression": "LIKE", "value": "erupt" }],
  "linkTreeVal": null, "vis": null }
```

`expression` is one of `EQ NEQ GT GTE LT LTE LIKE NOT_LIKE RANGE IN NOT_IN NULL NOT_NULL`. `RANGE` takes a
two-element array. The node must check `key` against its declared searchable fields and must never put
`key` or `sort.field` into a query unescaped.

```json
{ "pageIndex": 1, "pageSize": 10, "total": 42, "totalPage": 5,
  "list": [{ "id": 1, "title": "Hello", "category_name": "News" }] }
```

A reference column `@View(column = "name")` on field `category` is flattened to the key `category_name`.

### 7.3 Reference fields (`REFERENCE_TABLE` / `REFERENCE_TREE`)

The picker is requested under the **host** erupt's name, so the referenced model needs no menu:
`GET /erupt-api/build/{host}/{field}` returns the referenced model's `EruptBuildModel`, and
`POST /erupt-api/data/{host}/reference-table/{field}?tabRef=false[&dependValue=]` returns a `Page` of its rows.
The server does not rewrite names in the by-field build response.

With `referenceTableType` (or `referenceTreeType`) `{ "id": "id", "label": "name" }` on field `category`:

| Where | Value |
|-------|-------|
| add / update body, search condition value | `{"id": 2, "name": "Python"}` (an array of those for `IN`) |
| `GET /data/{erupt}/{id}` row | `"category": {"id": "2", "name": "Python"}` (id as a string) |
| table row | the same object plus the flattened column `"category_name": "Python"` (`field + "_" + view.column`, dots as `_`) |
| picker rows | must carry the `id` key and the label key (dots as `_`) |

`GET /erupt-api/data/{host}/reference-tree/{field}` returns nested nodes:
`[{ "id": "1", "label": "Tech", "pid": null, "root": true, "level": 1, "children": [ ... ] }]`.

### 7.4 Row operations

`eruptJson.rowOperation[]` carries only the operations visible to the acting user (the `show` expression is
evaluated on the node; the browser never sees it). `eruptClass` is the form erupt's local name, or `"void"`
when the operation has no form, and `build.operationErupts[code]` holds that form's `EruptModel`.

- `POST /erupt-api/data/{erupt}/operator/{code}/form-value?ids=` returns the form's initial values. A 404 is
  silently treated as an empty form.
- `POST /erupt-api/data/{erupt}/operator/{code}` with body `{"ids": [...], "param": {...} | null}` returns an `R`.
  The node validates `param` against the form and refuses a hidden operation. A truthy `data` is
  **executed as JavaScript** in the browser, so a node must only ever put code it authored there, never data.
- `callHint: ""` runs the operation without the confirm dialog.

### 7.5 `R` (write result)

```json
{ "success": true, "status": "SUCCESS", "message": null, "data": null, "promptWay": "MESSAGE" }
```

`status` is `SUCCESS | ERROR | INFO | WARNING`. `promptWay` is `MESSAGE | DIALOG | NOTIFY | NONE`.

## 8. Errors

| Case | Response |
|------|----------|
| Business / validation error shown to the user | HTTP 200, `R` with `success: false`, `status: "ERROR"`, a localized `message` |
| Browser origin | HTTP 403 |
| Unknown erupt, illegal nesting, unimplemented endpoint | HTTP 404 |
| Bad access token, missing right (§6.1) | HTTP 500 today (Java node throws). **[v1 proposal]** 403 |
| Unexpected failure | HTTP 500, `{ "path": "...", "status": 500, "error": "erupt exception", "message": "..." }` |

The server forwards non-200 bodies to the browser as they are, so a message must not contain stack
traces, SQL or secrets.

## 9. Versioning

Today the only version signal is `NodeInfo.version` (the erupt version). **[v1 proposal]**:

- Add `protocolVersion` (integer, starting at `1`) to `NodeInfo`. The server rejects a version it does not support.
- Within one `protocolVersion`, only additive changes: new optional request fields, new response
  fields, new Level 1/2 endpoints. Nodes ignore unknown fields.
- Removing or renaming a field, or changing its meaning, bumps `protocolVersion`.

## 10. Security checklist for node SDKs

- Reject any request with an `Origin` header. Compare `Access-Token` in constant time.
- Never expose the node port publicly. Only the server needs to reach it.
- Trust `user` only after `Access-Token` passes. **[v1 proposal]** Sign `user` with an HMAC keyed by the
  access token (plus a timestamp) so a leaked internal hop cannot impersonate users.
- Enforce §6.1 on every write. Apply tenant / row filters in queries.
- Whitelist `condition.key`, `sort.field` and every `{field}` path segment against the model. Parameterize all values.

## 11. Conformance **[v1 proposal]**

A Docker image bundling an erupt server and a protocol test suite. A node SDK passes Level 0 when it
registers, survives heartbeat expiry, and round-trips a reference model (table, search on every
`QueryExpression`, add, update, delete, power denial, nesting denial, origin rejection).

## 12. Open questions

1. Should `init-value` be optional (empty form) rather than Level 0?
2. `erupts` in the heartbeat is the only model inventory. Should a node also report a per-model hash so the server can tell when a model changed?
3. Excel import / export: keep it node-side, or let the server do it generically on top of Level 0 table + modify?
4. Attachments: node-local storage vs. server-provided storage that the node only references.
