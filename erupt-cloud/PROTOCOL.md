# Erupt Cloud Node Protocol — Draft v0

> Status: **draft**. Describes the wire contract between an erupt cloud **server** and a **node** as
> implemented today by `erupt-cloud-server` / `erupt-cloud-node`, so that a node can be written in any
> language. Sections marked **[v1 proposal]** are not implemented yet. §12 is protocol **v2**: a thinner node
> that leaves validation and translation to the server. The server side is implemented; no node SDK speaks it yet.

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
  "resources": {},
  "capabilities": ["invoke"]
}
```

| Field | Meaning |
|-------|---------|
| `instanceId` | Random id per process start. |
| `version` | The node's own release (an SDK's `python-0.2.0`, a Java node's erupt version), shown for diagnosis. The protocol is `protocolVersion` (§9). |
| `nodeAddress` | Base URLs the server should call. Must be reachable from the server. |
| `erupts` | Node-local erupt names. Unique within the node, no `.` allowed. |
| `eruptModules` / `resources` | Informational, shown on the server's node page. May be empty. |
| `capabilities` | Optional parts of the protocol the node serves: `invoke` (§5.5). Absent means none. |

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
| `rowOperation` | `invoke` kinds `operation` / `operation-form` (§5.5); a node without the `invoke` capability serves `POST /erupt-api/data/{erupt}/operator/{code}` and `POST .../operator/{code}/form-value` |
| `REFERENCE_TABLE` | none for a node with the `invoke` capability, the server reads the picker (§5.4); otherwise `POST /erupt-api/data/{erupt}/reference-table/{field}` (§7.3) |
| `TAB_TABLE_ADD` / tab editing | `POST /erupt-api/data/modify/tab-add|tab-update|tab-delete/{erupt}/{field}` |
| Options fetched by the node (`fetchHandler` named), `AUTO_COMPLETE`, `onchange`, `BUTTON` | `POST /erupt-api/invoke/{erupt}/{kind}/{field}` (§5.5), for a node registered with the `invoke` capability. A node without it (a Java node) is sent the Java endpoints instead: `comp/choice-item`, `comp/choice-item-filter`, `comp/tags-item`, `comp/auto-complete`, `data/onchange`, `comp/button` |
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
| Trees | `GET /erupt-api/data/tree/{erupt}`, `GET /erupt-api/data/depend-tree/{erupt}` | A `@Tree` erupt's records, paged through its `POST /data/table/{erupt}` (signed `power`), nested by `tree` `id` / `label` / `pid`, roots being the records with an empty parent. The tree beside a `@LinkTree` table is the records its `field` points at, nested by that field's `referenceTreeType`; a field the build does not carry is forwarded. Filtering the table by the picked nodes is the node's: `linkTreeVal` (§7.2). |
| Tab trees, checkboxes | `GET /erupt-api/data/tab/tree/{erupt}/{field}`, `GET /erupt-api/data/{erupt}/checkbox/{field}` | A `TAB_TREE` is the records its field points at, nested by that erupt's own `tree` (a field with a `filter` is forwarded). `CHECKBOX` is forwarded first, and on a 404 answered as `[{id, label, remark}]`: the target's primary key and the field's first `@View(column)`, `name` without one, since `checkboxType` is `@Transient`. A node stores either as the list of picked keys; the browser submits `[{id: key}]` (`CHECKBOX`) or `[{<pk>: key}]` (`TAB_TREE`). |
| Reference pickers | `POST /erupt-api/data/{erupt}/reference-table/{field}` | For a node with the `invoke` capability: the field's `fieldReturnName` read from the build, the query checked against that erupt's build and sent to its `POST /data/table/{erupt}` (signed `power,query`), references in the page resolved as a table's. With a `referenceTableType.dependField`, `dependValue` is required and added as `dependColumn = dependValue`; `dependColumn` is `@Transient` in Java, so a node SDK writes it into the build. A field the build does not carry is forwarded. |
| Field sub-models | `GET /erupt-api/build/{erupt}/{field}` | The field's `fieldReturnName` read from the build, then that erupt's own `GET /build/{erupt}` (signed `power`), returned as the node answers it. A field the build does not carry is forwarded. |
| Drag sort | `POST /erupt-api/data/modify/{erupt}/drag-sort` | Each row of the `{id: position}` body as a cell edit of the field `dragSort.field` names: read, patched, checked and saved whole through `update`, one row at a time. The field must be one of the model's fields. |
| View writes | `POST /erupt-api/data/modify/board/{erupt}/update_group`, `/calendar/{erupt}/update_date`, `/gantt/{erupt}/update_date`, `/gantt/{erupt}/link` | The `@Vis` the command's `visCode` names, read from the build; the row's group (and swimlane), dates or `ganttView.dependencyField` patched and saved whole as a cell edit is. A dependency is the predecessor's key, or a list of keys (objects carrying `id` are read too); a link that would close a cycle is refused. |
| Gantt links | `POST /erupt-api/data/modify/gantt/{erupt}/links` | Each requested row read through `GET /data/{erupt}/{id}` and its `dependencyField` answered as predecessor keys. |

A node answers **404** for anything it does not implement. The optional parts it does serve are listed in
`capabilities` at registration (§3.3). **[v1 proposal]** The server also hides the UI of what a node lists
it cannot serve, instead of letting it fail.

### 5.5 Invoke — a field's own code

A node registered with `"capabilities": ["invoke"]` (§3.3) serves one endpoint for every field that runs its
code, and the server turns the browser's calls into it:

```
POST /erupt-api/invoke/{erupt}/{kind}/{field}
{ "form": { ...the form's values... }, "value": "Pa" }
```

`value` only for `autocomplete`; `form` is `{}` when the browser sent none. For `operation` and `operation-form`
the last segment is the operation code and the body is the browser's own (`{ids, param}`). The answer is an `R` (§7.5):

| `kind` | Browser call | Declared in the build | `R.data` |
|--------|--------------|-----------------------|----------|
| `options` | `choice-item`, `choice-item-filter`, `tags-item` | the `fetchHandler` of `choiceType` / `multiChoiceType` / `tagsType` names anything | `[{"value", "label"}]`, or `["tag"]` for `TAGS` |
| `onchange` | `data/onchange` | `edit.onchange` is anything but `OnChange` | `{"formData": {}, "editExpr": {}}` |
| `autocomplete` | `comp/auto-complete?val=` (the server checks `triggerLength`) | `type: AUTO_COMPLETE` | `["suggestion"]` |
| `button` | `comp/button` | `type: BUTTON` | `{"eval": "js", "formData": {}, "editExpr": {}}` |
| `operation` | `data/{erupt}/operator/{code}` | `rowOperation` | as the operator endpoint answers (§7.4) |
| `operation-form` | `data/{erupt}/operator/{code}/form-value` | `rowOperation` with a form | the form's opening values; `null` without a form |

A refused call is `success: false` with its message; the server shows it. An unknown field or `kind` is 404.

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
node applies that view's own `filter` and, when no `sort` is sent, its `orderBy`, both kept out of the build. With
a `@LinkTree`, `linkTreeVal` holds the tree keys the browser picked: the node keeps the rows
whose `linkTree.field` is one of them (`IN`), and answers an empty page when `linkTree.dependNode` is set and
nothing is picked.

Under a signed `Erupt-Enforced: query` the server has checked every condition the browser sent against the
build, and may add its own (a dependent picker's): the node then takes a condition on any stored field, still
checking its operator and value.

A drill (`@Erupt(drills)`) opens the linked erupt's table with the headers `Drill` (the drill's code),
`DrillValue` (the key of the source row) and `DrillSourceErupt` (the source erupt). The node reads the source
row, takes its `link.column` (`id` by default) and keeps the rows whose `link.joinColumn` holds it, plus
`link.linkCondition` as written; `column` and `joinColumn` are `@Transient`, so they stay on the node. A drill
the source erupt does not declare, or whose `linkErupt` is not the table read, is refused.

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

`NodeInfo.protocolVersion` (integer) names the protocol a node instance speaks; the server refuses a version it
does not know and answers every registration with the newest it speaks (§12.2).

- Within one `protocolVersion`, only additive changes: new optional request fields, new response
  fields, new Level 1/2 endpoints. Nodes ignore unknown fields.
- Removing or renaming a field, or changing its meaning, bumps `protocolVersion`.

A node that sends no `protocolVersion` speaks version 1, everything above §12. Version 2 is §12.

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

## 12. Protocol v2 — thin nodes **[v2: the server and every erupt-sdk node; the Java node speaks v1]**

### 12.1 Goal

In version 1 every node SDK repeats what the server already does for its own erupts: it validates its
annotation JSON against `schema/` and completes it with defaults, checks the user's power, checks queries and
records, coerces values, and turns drills and link trees into conditions. The server does all of that too
whenever it sends `Erupt-Enforced` (§4.2); the node keeps its copy only for servers that do not.

In version 2 the server always does it, once, in Java, and a node receives **signed, checked and
normalized commands**. What remains on a node is what only the node can do:

| Node (v2) | Server (v2) |
|-----------|-------------|
| Declare models in its own language; resolve `EditType.AUTO` and `DateType.AUTO`; evaluate its expressions | Complete the declared JSON with defaults and validate it against `schema/` (§12.3) |
| Verify the signature | Sign every call; never send `Access-Token` to a v2 node |
| Store and read rows; apply tenant / row scope (§6.2) | Enforce the user's power **and** the node's declared power (§12.5) |
| Apply a `@Vis` view's `filter` / `orderBy`, a drill's `linkCondition`, `Erupt.orderBy`: the node's query language | Check queries and records; normalize every value (§12.4) |
| Run DataProxy hooks, row operations and `invoke` code | Turn drills, link trees and dependent pickers into plain conditions (§12.4) |

Everything else in this document holds for v2 unless §12 says otherwise: lifecycle (§3), naming (§4.4),
the endpoint list (§5), the endpoints the server answers itself (§5.4), `Page` and `R` (§7), errors (§8).

### 12.2 Negotiation

- A v2 node registers with `"protocolVersion": 2` (§3.3). A server that speaks v2 answers every register call
  with the newest version it speaks, `{"protocolVersion": 2}`, and refuses a version it does not know. A node
  that gets no such answer is talking to an older server: it logs an error naming the server version it needs
  and stops registering. It never falls back to v1.
- Every instance of a node speaks the same version. While a node has live instances, the server refuses to
  register an instance speaking another one: upgrade a node by replacing its instances (stop the old ones, which
  deregister, then start the new), not by mixing versions.
- Every call to a v2 node is signed (§4.3) and carries no `Access-Token`. A v2 node refuses (403) an unsigned
  call, and one whose `Erupt-Enforced` lacks what the endpoint needs:

  | Endpoint | Required in `Erupt-Enforced` |
  |----------|------------------------------|
  | `GET /build/{erupt}` | nothing: the server reads builds on its own account |
  | `POST /data/table/{erupt}`, `aggregate`, `extra-row`, Excel export | `power,query` |
  | `POST /data/modify/{erupt}`, `.../update`, `.../tab-add`, `.../tab-update`, `POST /data/form-view/{erupt}` | `power,record` |
  | everything else | `power` |

  This per-call check is what makes it safe for a node to skip the work below; a node never relies on the
  negotiation alone.
- The `erupt-power` callback (§6) stays available but a v2 node has no reason to call it.

### 12.3 Build

A v2 node answers `GET /build/{erupt}` with only what it **declares**: in `eruptJson` and every
`eruptFieldJson`, the attributes it sets; in `power`, the rights it switches off. The server:

1. completes every object from `erupt-node-defaults.json` and the annotation defaults
   (`AnnotationProcess.completeJson`, today applied to fields only by `completeFields`), including
   `eruptJson`, `power`, `rowOperation` and the `operationErupts`, `tabErupts` and `combineErupts`;
2. reads every attribute back as the annotation it encodes, the types `erupt-annotations.schema.json` is
   generated from: an enum value the annotation does not declare, a number where a boolean goes, an object
   where an array goes fails. An invalid build is a node bug: the erupt answers an error naming the model, the
   attribute path and the offending value, and nothing is cached;
3. keeps the attributes the browser must not see (below) for itself and strips them from the build it sends on;
4. caches the result as today (§5.4, one minute per language).

A `@ToMap` attribute (`Erupt.param`, `EruptField.params`) is declared as the object it travels as.

Attributes that are `@Transient` in Java and that a v2 node now **sends** in the build, because the server acts
on them:

| Attribute | Used for |
|-----------|----------|
| `drills[].link.column`, `drills[].link.joinColumn` | Drill conditions (§12.4) |
| `referenceTableType.dependColumn` (as today, §5.4) | Dependent picker conditions |

`drills[].link.linkCondition`, a `@Vis` view's `filter` and `orderBy` and `Erupt.orderBy` stay on the node:
they are written in the node's query language.

The node still resolves `EditType.AUTO` and `DateType.AUTO` from its own types, evaluates the `show` of its
row operations for the acting user (§7.4), and fills `componentValue` for options it computes. The other AUTO
values a local erupt's proxies resolve are the server's, resolved by the same rules after completion: a view's
`type` by the edit (`ViewProxy`), then by `fieldReturnName` (`Boolean`, `Date` by `dateType`, `number`, else
`TEXT`); `search.operator` (`RANGE` for a date, `EQ` otherwise, `SearchProxy`); `boolType.type` (`SWITCH` when
`notNull`, `RADIO` otherwise, `BoolTypeProxy`). A node declares none of them unless it means a specific value.

### 12.4 Normalized requests

The bodies keep their v1 shape; their **values** arrive normalized, and a few headers and fields are replaced
by plain conditions.

**Values.** In a search condition, a record, an operation's `param` and its `ids`, every value is the JSON type
its field stores:

| Field | Value |
|-------|-------|
| Primary key in a record | Typed like the primary key column (`fieldReturnName`): a number for `"number"`, a string otherwise |
| `NUMBER`, `RATE`, `SLIDER` | JSON number |
| `BOOLEAN` | `true` / `false` |
| `DATE` by `dateType.type` | String: `DATE` `yyyy-MM-dd`, `DATE_TIME` `yyyy-MM-dd HH:mm:ss`, `TIME` `HH:mm:ss`, `MONTH` `yyyy-MM`, `YEAR` `yyyy`. A fraction of a second is dropped, a time zone refused. `WEEK`, `QUARTER` and a type left `AUTO` travel as the picker wrote them (§13) |
| `REFERENCE_TABLE`, `REFERENCE_TREE` | The referenced key alone (the `id` of `referenceTableType` / `referenceTreeType`), typed like the target's primary key; never `{id, label}` (§7.3 for v1). A target the node does not serve keeps the key as sent |
| `CHECKBOX`, `TAB_TREE` | The array of picked keys, typed like the target's primary key (the browser's `[{id: key}]` unwrapped) |
| `MULTI_CHOICE`, `TAGS`, `TRANSFER` | The array of values, each a scalar |
| `TAB_TABLE_ADD`, `TAB_TABLE_REFER`, `MULTI_FORM` rows, a `COMBINE` form | As the browser sent it (§13) |
| Anything else | String |

A value is never an object or an array where the table above has one value: the server refuses the request
(`incorrect format` for a record, `Illegal search condition` for a query) rather than pass on something a node
could read as part of its query.

A primary key in a path segment (`GET /data/{erupt}/{id}`), the `delete` body and an operation's `ids` arrive as
the browser sent them: the node reads them as its key type.

**Conditions.** `expression` keeps its meaning, with fixed value shapes:

| `expression` | `value` |
|--------------|---------|
| `EQ` `NEQ` `GT` `GTE` `LT` `LTE` | One value |
| `LIKE` `NOT_LIKE` | The text the user typed. The node adds the wildcards and escapes it (§10); a `%` it holds is a literal |
| `RANGE` | `[low, high]`, both present |
| `IN` `NOT_IN` | A non-empty array (a single value is wrapped) |
| `NULL` `NOT_NULL` | Absent |

`expression` is always present: a condition the browser sends without one (or with `AUTO`) gets the field's
`search.operator`, and `AUTO` there means `RANGE` for a date and `EQ` otherwise, as for a local erupt. A
condition with an empty value matches everything and is left out. The browser's conditions are only on
searchable fields (§4.2); the ones the server adds (below) may be on any stored field.

**Translated by the server.** A v2 node no longer receives these; the server reads what it needs through the
node's own endpoints and sends plain conditions instead:

| v1 input | v2 |
|----------|----|
| `Drill`, `DrillValue`, `DrillSourceErupt` headers | The server checks the drill against the source erupt's build (declared under that code, `linkErupt` the table read, both on this node), reads the source row (`GET /data/{source}/{id}`, signed `power`), and adds `EQ` on `link.joinColumn` with the row's `link.column` (`NULL` when the row has none; a source row the node does not return, `null` or `{}`, refuses the drill);
a `joinColumn` written through the reference (`author.id`) names the reference field (`author`), which a node stores as the key. The body carries `"drill": {"erupt": "<source>", "code": "<code>"}` so the node adds that drill's `linkCondition` itself; the headers are not sent. |
| `linkTreeVal` | `IN` on `linkTree.field` with the picked keys. With `linkTree.dependNode` and nothing picked, `NULL` on the primary key, which no row matches. `linkTreeVal` is not sent. |
| `dependValue` of a reference picker | `EQ` on `dependColumn`, as for a node with `invoke` (§5.4); a v2 node always counts as one |

**Table query.**

```json
{ "pageIndex": 1, "pageSize": 10,
  "sort": [{ "field": "createTime", "direction": "DESC" }],
  "condition": [{ "key": "category", "expression": "IN", "value": [2, 3] }],
  "vis": "board", "drill": { "erupt": "Category", "code": "articles" } }
```

`sort` is checked as in v1 (§4.2): a sortable column, the primary key, or `field.column` of a reference. Without
`sort`, the node orders by the `@Vis` view's `orderBy`, then `Erupt.orderBy`, then the primary key ascending.

`drill` is the server's alone: one the browser writes into the body is dropped, and a query carries only
`pageIndex`, `pageSize`, `sort`, `condition`, `vis` and `drill`.

**Records.** An add or update body carries the model's stored fields only, normalized; the primary key is
present for an update and absent or ignored for an add. Unknown keys are dropped by the server. The same holds,
signed `power,record`, for a form view's save (`POST /data/form-view/{erupt}`, which also needs `add` or `edit`)
and a sub-table row (`POST /data/modify/tab-add|tab-update/{erupt}/{field}`), checked against the erupt the
`TAB_TABLE_ADD` field holds (`build.tabErupts[field]`).

**Row operations.** A v2 node serves them through `invoke` (§5.5), whatever its `capabilities` list. `param`
has been checked as a record against the operation's form (`build.operationErupts[code]`) and normalized. A node
still refuses an operation whose `show` hides it from the acting user (§7.4).

### 12.5 Power

The server enforces, on every call, the user's rights **and** the rights the node declares in its build: a
model whose build switches `delete` off cannot be deleted from, whatever the user holds. In v1 the declared
half is the node's job (§4.2). The declared rights are read from the server's cached build, which is per
language and not per user: a node that switches rights per user still checks those itself.

This holds for the server's in-process callers too (AI tools, workflows reaching a node erupt through
`EruptRemoteRouter`): for a v2 node the server checks the acting user's rights, the query or the record, and
for an update the stored row patched with the values sent, before it forwards.

### 12.6 What a v2 node keeps doing

- Verify the signature and the `Erupt-Enforced` values of §12.2; reject an `Origin` header (§10).
- Check every field name it puts into a query (`condition.key`, `sort.field`, record keys, `{field}` path
  segments) against its stored fields, and bind every value as a parameter. This is a membership test, not a
  validation: the server has already validated. It stays because a node never trusts a name into SQL.
- Apply tenant and row scope (§6.2), a `@Vis` view's `filter`, a drill's `linkCondition`, the default order.
- Run DataProxy hooks and row operations; answer rows with references as their key (§7.3).

### 12.7 What a v2 node drops

| v1 duty | Where it went |
|---------|---------------|
| Validating its annotation JSON against `schema/`, merging `erupt-node-defaults.json` (an SDK no longer embeds either file at run time) | Server, §12.3 |
| `Access-Token` comparison | Signature only, §12.2 |
| `erupt-power` callback and the §6.1 checks | Server, §12.5 |
| Checking a query's fields, expressions and page size; checking a record (not-null, numbers, length, choices) | Server, §4.2 |
| Coercing values (dates, numbers, reference objects to keys, `RANGE` / `IN` shapes), resolving an omitted operator | Server, §12.4 |
| Drill headers, `linkTreeVal`, `dependValue` | Server, §12.4 |

### 12.8 Server implementation

| Part | Where |
|------|-------|
| Version on the wire, one per node; the register answer | `CloudProtocol`, `NodeInfo.protocolVersion`, `RegisterAnswer` (erupt-cloud-common); `MetaNode.protocol()`, `EruptMicroserviceController`, `EruptNodeMicroservice` |
| Build completion and check | `AnnotationProcess.completeJsonStrict` (erupt-core, `@ToMap` read back), `NodeBuilds` (with the AUTO values of `ViewProxy.byEdit`, `SearchProxy.byDate`, `BoolTypeProxy.byNotNull`); applied in `CloudNodeRouter.resolveBuild` and the forwarded build |
| Values, conditions, link trees | `NodeRequestNormalizer`, applied in the interceptor for the browser's calls and in `CloudNodeRouter` for every call the server makes |
| Drills | `NodeDrillService` |
| Declared power; in-process callers | `EruptCloudServerInterceptor`, `CloudNodeRouter` (`tableQuery`, `findById`, `insert`, `update`, `delete`) |
| No `Access-Token`, no drill headers | `EruptCloudServerInterceptor.httpProxy`, `CloudNodeRouter.exchange` |

Tests: `NodeRequestNormalizerTest`, `NodeBuildsTest`, `NodeDrillServiceTest`, `NodeManagerTest` (erupt-cloud-server).

### 12.9 Conformance

`conformance/run.py` (in erupt-sdk) checks a node registering with `protocolVersion: 2` as a v2 node: it answers
the registration with `{"protocolVersion": 2}`, sends only signed, normalized calls, and checks that the node
refuses an unsigned call and one missing a required `Erupt-Enforced` value. Search, drill and link tree checks
send the conditions of §12.4 instead of headers and `linkTreeVal`. The declared build is checked against the
annotation schema and completed as `NodeBuilds` does (`conformance/annotations.py`) before the build assertions.
Any other node is checked against v1, as the Java node is.

## 13. Open questions

1. Should `init-value` be optional (empty form) rather than Level 0?
2. `erupts` in the heartbeat is the only model inventory. Should a node also report a per-model hash so the server can tell when a model changed?
3. ~~Excel import / export~~: done by the server on a 404 (§5.3). Open: an optional batch add, so an import
   can be all-or-nothing on a node.
4. ~~Attachments~~: stored by the server, the node only references the path (§5.4).
5. v2: a wire format for `WEEK` and `QUARTER` dates, sent today as the picker writes them.
6. v2: `MULTI_CHOICE` / `TAGS` travel as the browser sends them. A Java node stores one joined string; a fixed
   wire form (an array) would need the build to say which the node stores.
7. v2: should the server also compute `componentValue` for static options and evaluate the `show` of row
   operations, if a node sends those expressions in a form the server can run? Today both need node code.
