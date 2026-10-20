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
| `Erupt-Timestamp`, `Erupt-Signature` | Request signature, see §4.3 | Verify; a present but invalid signature → 403. |
| `Erupt-Enforced` | Checks the server ran before forwarding, see §4.2 (covered by the signature) | May skip those checks. |
| `Access-Token` | The node's access token; omitted when the server runs with `plain-access-token: false` | Without a valid signature: compare (constant time) with its own; mismatch → reject. |
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

Always: the login, and that the user holds the **menu** for the erupt (or for its parent).

A server that sends `Erupt-Enforced` (erupt 2.3.x and later) has also run, for the listed values:

| Value | Checked before forwarding |
|-------|---------------------------|
| `power` | The user's rights for the endpoint (`@EruptRouter(power = ...)`: add, edit or view-details, delete, export, import), refused with the standard "insufficient permissions" error; and the `power` of a build response restricted to the user's rights. |
| `query` | A `TableQuery` against the build model: conditions only on searchable fields with a real operator (references: `EQ` `NEQ` `IN` `NOT_IN` `NULL` `NOT_NULL`), sorts only on sortable columns or the primary key, page size at most 1000 or the largest the model offers. |
| `record` | An added or updated record: not-null, numbers, textarea length, choice options and XSS, with the messages of a local erupt. |

Without the header (older servers) a node enforces §6.1 itself. With `power` it may skip the
`erupt-power` callback entirely; its own declared power (a model with delete switched off) is still its
job. A node keeps whitelisting field names it puts into SQL even when `query` is listed.

### 4.3 Request signature

Every forward carries an HMAC-SHA256 keyed by the node's access token, hex encoded:

```
Erupt-Signature = HMAC(accessToken, METHOD \n path?query \n timestamp \n enforced \n sha256(user) \n sha256(body))
```

`path?query` is the path the node is called on, below any context path; `enforced` is the
`Erupt-Enforced` value or empty; `user` is the raw `user` header (empty when absent); the body digest is
of the raw bytes (an empty body digests the empty string) or the literal `UNSIGNED-PAYLOAD` for a
multipart body (an Excel import; attachments stay on the server, §5.4), which the forward re-encodes. A node accepts a timestamp within 300 seconds of its
clock. The server signs every call, including its own (`EruptRemoteRouter`), and also sends `Access-Token`
until it is configured with `erupt.cloud-server.plain-access-token: false`; a Java node refuses unsigned
calls when `erupt.cloud-node.require-signature: true`. The reference implementation is
`xyz.erupt.cloud.common.http.CloudSignature`.

### 4.4 Naming

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
| `REFERENCE_TABLE` | `POST /erupt-api/data/{erupt}/reference-table/{field}` (§7.3) |
| `CHECKBOX` | `GET /erupt-api/data/{erupt}/checkbox/{field}` |
| `TAB_TREE` | `GET /erupt-api/data/tab/tree/{erupt}/{field}` |
| `TAB_TABLE_ADD` / tab editing | `POST /erupt-api/data/modify/tab-add|tab-update|tab-delete/{erupt}/{field}` |
| `CHOICE` / `MULTI_CHOICE` / `TAGS` options fetched by a handler | `GET /erupt-api/comp/choice-item/{erupt}/{field}`, `POST /erupt-api/comp/choice-item-filter/{erupt}/{field}`, `POST /erupt-api/comp/tags-item/{erupt}/{field}`; only for a field whose build names a `fetchHandler`, the server answers the others (§5.4) |
| `AUTO_COMPLETE` | `POST /erupt-api/comp/auto-complete/{erupt}/{field}` |
| `onchange` | `POST /erupt-api/data/onchange/{erupt}/{field}` |
| `BUTTON` | `POST /erupt-api/comp/button/{erupt}/{field}` |
| view `statistic` | `POST /erupt-api/data/aggregate/{erupt}` |
| extra rows | `POST /erupt-api/data/extra-row/{erupt}` |
| form-view layout | `GET`/`POST /erupt-api/data/form-view/{erupt}` |

### 5.3 Level 2 — optional extensions

| Feature | Endpoints |
|---------|-----------|
| Excel import / export | `/erupt-api/excel/template|export|import/{erupt}`; a 404 leaves them to the server (below) |
| Template pages | any `/erupt-api/tpl/...` path, routed by the `_node` query param, forwarded verbatim |

**Excel by the server.** When a node answers 404, the server writes the template from the build model,
exports by paging through `POST /data/table/{erupt}` (signed `power,query`; selected ids through
`GET /data/{erupt}/{id}`), with references resolved as in §7.3 and choices labelled from the build's
`componentValue`, and imports by parsing the file, mapping labels back to keys (references through the
referenced erupt's table, signed `power`), checking each row as a submitted record and adding it through
`POST /data/modify/{erupt}` (signed `power,record`). Rows are added one by one: an import stops at the first
failing row, keeps the rows before it and says how many there were.

### 5.4 Done by the server

Endpoints the server answers for a node itself, from the node's build (fetched with the server's own signed
`GET /build/{erupt}` and cached for a minute); nothing is forwarded:

| Feature | Endpoints | How |
|---------|-----------|-----|
| Attachments, rich-text images | `POST /erupt-api/file/upload/{erupt}/{field}`, `/uploads/...`, `/upload-html-editor/...`, `/upload-ueditor/...` | Stored by the server under the field's `attachmentType` (file types, size, image dimensions, path) or `htmlEditorType.path`, after the user's `add` / `edit` right is checked; the download (`/erupt-api/file/download-attachment`, `/erupt-attachment`) serves them from the same storage. The node stores the returned path like any string column (several joined by `attachmentType.fileSeparator`). |
| Static options | `choice-item`, `choice-item-filter`, `tags-item` of a field whose `choiceType` / `multiChoiceType` / `tagsType` names no `fetchHandler` | The build's `componentValue`. A field naming a handler is forwarded; a 404 then falls back to the same `componentValue`. |
| Cell editing | `POST /erupt-api/data/modify/{erupt}/update-cell` | When the build's power allows `cellEdit` and the field's `edit` has a title, `cellEdit` and is not read-only: the row read through `GET /data/{erupt}/{id}` (signed `power`), patched, checked as a record, and saved whole through `POST /data/modify/{erupt}/update` (signed `power,record`). A node therefore leaves `cellEdit` as declared instead of turning it off. |
| Reference trees | `GET /erupt-api/data/{erupt}/reference-tree/{field}` | Built from the referenced erupt's records, paged through `POST /data/table/{erupt}` (signed `power`) and nested by `referenceTreeType` `id` / `label` / `pid`; with a `dependField`, only the records whose `dependColumn` holds the `dependValue` (a reference sent as its key counts as its `id`). A field with a `filter` or an `orderBy` is forwarded instead: those are the node's query language. |
| Field sub-models | `GET /erupt-api/build/{erupt}/{field}` | The field's `fieldReturnName` read from the build, then that erupt's own `GET /build/{erupt}` (signed `power`), returned as the node answers it. A field the build does not carry is forwarded. |
| Drag sort | `POST /erupt-api/data/modify/{erupt}/drag-sort` | Each row of the `{id: position}` body as a cell edit of the field `dragSort.field` names: read, patched, checked and saved whole through `update`, one row at a time. The field must be one of the model's fields. |
| View writes | `POST /erupt-api/data/modify/board/{erupt}/update_group`, `/calendar/{erupt}/update_date`, `/gantt/{erupt}/update_date`, `/gantt/{erupt}/link` | The `@Vis` the command's `visCode` names, read from the build; the row's group (and swimlane), dates or `ganttView.dependencyField` patched and saved whole as a cell edit is. A dependency is the predecessor's key, or a list of keys (objects carrying `id` are read too); a link that would close a cycle is refused. |
| Gantt links | `POST /erupt-api/data/modify/gantt/{erupt}/links` | Each requested row read through `GET /data/{erupt}/{id}` and its `dependencyField` answered as predecessor keys. |

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

### 6.1 Permissions on the node (servers without `Erupt-Enforced: power`)

The server enforces the user's rights itself when it says so (§4.2). For older servers, every request
the node computes the effective power as **declared power AND server power**:

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
`key` or `sort.field` into a query unescaped. `vis` is the `code` of the `@Vis` view the table is read for
(a view without one is addressed by its title's hash in Java, by its title in the SDKs, as its build says): the
node applies that view's own `filter` and, when no `sort` is sent, its `orderBy`, both kept out of the build.

```json
{ "pageIndex": 1, "pageSize": 10, "total": 42, "totalPage": 5,
  "list": [{ "id": 1, "title": "Hello", "category_name": "News" }] }
```

A reference column `@View(column = "name")` on field `category` is flattened to the key `category_name`.

### 7.3 Reference fields (`REFERENCE_TABLE` / `REFERENCE_TREE`)

The picker is requested under the **host** erupt's name, so the referenced model needs no menu:
`GET /erupt-api/build/{host}/{field}` returns the referenced model's `EruptBuildModel` (answered by the server,
§5.4), and `POST /erupt-api/data/{host}/reference-table/{field}?tabRef=false[&dependValue=]` returns a `Page`
of its rows. The server does not rewrite names in the by-field build response.

With `referenceTableType` (or `referenceTreeType`) `{ "id": "id", "label": "name" }` on field `category`:

| Where | Value |
|-------|-------|
| add / update body, search condition value | `{"id": 2, "name": "Python"}` (an array of those for `IN`) |
| `GET /data/{erupt}/{id}` row, table row | the key alone, `"category": 2`; the server completes it (below) |
| picker rows | must carry the `id` key and the label key (dots as `_`) |

**Resolved by the server.** A node answers rows with references as their key. For the table and get-by-id
endpoints the server reads each distinct key once from the referenced erupt (`GET /data/{target}/{id}`,
signed with `Erupt-Enforced: power`) and puts back what the browser reads: `"category": {"id": "2",
"name": "Python"}` (id as a string) and, for every `@View(column)` of the field, the flattened
`"category_name": "Python"` (`field + "_" + view.column`, dots as `_`). A key that does not resolve keeps
its id and gets a null label. A reference that already is an object (a Java node) is left untouched.

`GET /erupt-api/data/{host}/reference-tree/{field}` returns nested nodes:
`[{ "id": "1", "label": "Tech", "pid": null, "root": true, "level": 1, "children": [ ... ] }]`, built by the
server (§5.4) from the referenced erupt's table (signed with `Erupt-Enforced: power`, at most 10000 records).

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

- Reject any request with an `Origin` header. Verify `Erupt-Signature` (§4.3); otherwise compare `Access-Token` in constant time.
- Never expose the node port publicly. Only the server needs to reach it.
- Trust `user` only after the signature (which covers it) or `Access-Token` passes.
- Enforce §6.1 on every write unless the server lists `power` in `Erupt-Enforced`. Apply tenant / row filters in queries.
- Whitelist `condition.key`, `sort.field` and every `{field}` path segment against the model. Parameterize all values.

## 11. Conformance **[v1 proposal]**

A Docker image bundling an erupt server and a protocol test suite. A node SDK passes Level 0 when it
registers, survives heartbeat expiry, and round-trips a reference model (table, search on every
`QueryExpression`, add, update, delete, power denial, nesting denial, origin rejection).

## 12. Open questions

1. Should `init-value` be optional (empty form) rather than Level 0?
2. `erupts` in the heartbeat is the only model inventory. Should a node also report a per-model hash so the server can tell when a model changed?
3. ~~Excel import / export~~: done by the server on a 404 (§5.3). Open: an optional batch add, so an import
   can be all-or-nothing on a node.
4. ~~Attachments~~: stored by the server, the node only references the path (§5.4).
