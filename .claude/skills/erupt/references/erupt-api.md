# Erupt Core REST API 参考

所有接口前缀 `/erupt-api`，鉴权通过 `@EruptRouter` 注解控制（Token 默认放 Header）。

---

## 路径参数的反射来源

接口路径中的 `{erupt}`、`{field}`、`{tabName}`、`{fieldName}` 等参数**不是自由填写的字符串**，必须通过 Erupt 对象模型反射获取，填错会导致 404 或权限校验失败。

| 路径参数 | 来源 | 说明 |
|---------|------|------|
| `{erupt}` | `EruptModel.eruptName` | 即 `@Erupt` 所标注的 Java 类的**简单类名**（`Class.getSimpleName()`），如 `EruptUser`、`Order` |
| `{field}` / `{fieldName}` | `EruptFieldModel.fieldName` | `@EruptField` 所标注的 Java **字段名**（非 `@View.title`、非数据库列名），如 `userType`、`dept` |
| `{tabName}` | Tab 字段的字段名 | 与 `{fieldName}` 相同，取 `TAB_TABLE_ADD` / `TAB_TABLE_REFER` / `TAB_TREE` 类型字段的 Java 字段名 |
| `{code}` | `@RowOperation.code` | 行操作注解上显式声明的 `code` 值 |

### 获取方式

前端通过 `/erupt-api/build/{erupt}` 拿到完整模型后，可从中提取所有合法的字段名：

```
EruptBuildModel
  └── eruptModel.eruptFieldModels[]
        └── fieldName        ← 即接口中的 {field} / {fieldName}
  └── tabErupts              ← key 即 {tabName}
  └── operationErupts        ← key 即 {code}（对应 @RowOperation.code）
```

### 反例

```
# 错误：用了数据库列名
POST /erupt-api/data/modify/EruptUser
body: { "user_name": "tom" }   ❌  应为字段名 "username"

# 错误：用了 @View.title
GET /erupt-api/comp/choice-item/EruptUser/用户类型  ❌  应为字段名 "userType"

# 正确
GET /erupt-api/comp/choice-item/EruptUser/userType  ✅
```

---

## 通用响应模型

### `R<T>` — 通用响应
```
success: boolean
status: SUCCESS | ERROR
data:    T
message: string（可选）
promptWay: MESSAGE | DIALOG | NOTIFY | NONE
```

### `EruptApiModel` — 操作结果
```
status:    SUCCESS | ERROR | INFO | WARNING
message:   string
data:      Object
promptWay: MESSAGE | DIALOG | NOTIFY | NONE
```

### `Page` — 分页响应
```
pageIndex: int
pageSize:  int
total:     long
totalPage: int
list:      Collection<Map<String,Object>>
sort:      List<Sort>
alert:     Alert（可选）
```

---

## Build — UI 结构

### `GET /erupt-api/build/{erupt}`
返回指定 Erupt 实体的完整 UI 结构（模型、权限、字段、Tab、操作按钮）。

**Path:** `erupt` = Erupt 类名（如 `EruptUser`）

**Response: `EruptBuildModel`**
```
eruptModel:      EruptModel          // 主实体模型
tabErupts:       Map<String, EruptBuildModel>  // Tab 字段子模型
combineErupts:   Map<String, EruptModel>       // COMBINE 字段模型
operationErupts: Map<String, EruptModel>       // 行操作表单模型
power:           PowerObject         // 当前用户权限
```

### `GET /erupt-api/build/{erupt}/{field}`
获取某个字段所引用的 Erupt 的 UI 结构（用于 TAB_TABLE、TAB_TREE 等类型）。

---

## Data — 数据查询

### `POST /erupt-api/data/table/{erupt}`
分页查询表格数据。

**Body: `TableQuery`**
```json
{
  "pageIndex": 1,
  "pageSize": 20,
  "sort": [{"prop": "createTime", "order": "descending"}],
  "condition": [{"key": "status", "value": "1", "conditionType": "EQ"}],
  "linkTreeVal": [],
  "vis": ""
}
```
**Response:** `Page`

---

### `GET /erupt-api/data/tree/{erupt}`
获取树形结构数据（需配置 `@Tree`）。

**Response:** `Collection<TreeModel>` — `{id, label, pid, children[]}`

---

### `GET /erupt-api/data/init-value/{erupt}`
获取新增表单的默认初始值（触发 `DataProxy.beforeAdd` 中的默认值逻辑）。

**Response:** `Map<String, Object>`

---

### `GET /erupt-api/data/{erupt}/{id}`
按主键查询单条数据。

**Response:** `Map<String, Object>`

---

### `POST /erupt-api/data/{erupt}/operator/{code}/form-value`
获取行操作弹窗表单的初始值。

**Params:** `ids`（可选，`List<Object>`，选中行的主键列表）

**Response:** `Object`

---

### `POST /erupt-api/data/{erupt}/operator/{code}`
执行行操作（对应 `@RowOperation`）。

**Body:**
```json
{
  "param": { /* 表单数据 */ },
  "ids":  ["id1", "id2"]
}
```
**Response:** `EruptApiModel`

---

### `POST /erupt-api/data/extra-row/{erupt}`
获取 `DataProxy.extraRow()` 返回的额外行数据。

**Body:** `TableQuery`（同分页查询）

**Response:** `List<Row>`

---

### `POST /erupt-api/data/onchange/{erupt}/{field}`
字段值变化回调，返回联动字段更新。

**Body:** `JsonObject`（当前表单所有字段值）

**Response:**
```json
{
  "success": true,
  "data": {
    "formData": { /* 需要更新的字段值 */ },
    "editExpr": { /* 字段显示/隐藏表达式 */ }
  }
}
```

---

### `GET /erupt-api/data/tab/tree/{erupt}/{tabFieldName}`
获取 Tab 字段（TAB_TREE 类型）的树数据。

**Response:** `Collection<TreeModel>`

---

### `GET /erupt-api/data/{erupt}/checkbox/{fieldName}`
获取 CHECKBOX 类型字段的选项列表。

**Response:** `Collection<CheckboxModel>` — `{id, label, checked}`

---

### `POST /erupt-api/data/{erupt}/reference-table/{fieldName}`
分页查询 REFERENCE_TABLE 字段的候选数据。

**Params:**
- `dependValue`（可选）— 级联依赖值
- `tabRef`（可选，boolean）— 是否为 Tab 场景

**Body:** 同 `TableQuery`

**Response:** `Page`

---

### `GET /erupt-api/data/{erupt}/reference-tree/{fieldName}`
获取 REFERENCE_TREE 字段的树数据。

**Params:** `dependValue`（可选）

**Response:** `Collection<TreeModel>`

---

### `GET /erupt-api/data/depend-tree/{erupt}`
获取 `@LinkTree` 依赖树数据。

**Response:** `Collection<TreeModel>`

---

## Modify — 数据写入

所有写入接口均记录操作日志（`@EruptRecordOperate`），并触发 `DataProxy` 生命周期钩子。

### `POST /erupt-api/data/modify/{erupt}`
新增实体。

**Body:** `JsonObject`（字段键值对）

**Response:** `R<Void>`

---

### `POST /erupt-api/data/modify/{erupt}/update`
更新实体（Body 中需包含主键）。

**Body:** `JsonObject`（含主键的字段键值对）

**Response:** `R<Void>`

---

### `POST /erupt-api/data/modify/{erupt}/delete`
删除实体（支持批量）。

**Body:** `List<Object>`（主键列表）

**Response:** `R<Void>`

---

### Tab 子表操作

#### `POST /erupt-api/data/modify/tab-add/{erupt}/{tabName}`
向 Tab 集合字段新增一条子记录。

**Body:** `JsonObject`

**Response:** `EruptApiModel`（data 为更新后的对象）

---

#### `POST /erupt-api/data/modify/tab-update/{erupt}/{tabName}`
更新 Tab 集合字段中的子记录。

**Body:** `JsonObject`（含子记录主键）

**Response:** `EruptApiModel`

---

#### `POST /erupt-api/data/modify/tab-delete/{erupt}/{tabName}`
删除 Tab 集合字段中的子记录。

**Body:** `JsonObject`（含子记录主键）

**Response:** `EruptApiModel`

---

### Gantt 日期更新

#### `POST /erupt-api/data/modify/gantt/{erupt}/update_date`
更新甘特图条目的时间范围。

**Body:**
```json
{
  "visCode":   "gantt_vis",
  "pk":        "123",
  "startDate": "2026-01-01",
  "endDate":   "2026-03-31"
}
```
**Response:** `R<Void>`

---

## Comp — 组件数据

### `POST /erupt-api/comp/auto-complete/{erupt}/{field}`
自动完成组件输入过滤。

**Params:** `val` — 用户输入文本

**Body:** `Map<String, Object>`（可选，当前表单数据）

**Response:** `List<Object>`

---

### `GET /erupt-api/comp/choice-item/{erupt}/{field}`
获取 CHOICE / MULTI_CHOICE 的选项列表。

**Response:** `List<VLModel>` — `{value, label, color, ...}`

---

### `POST /erupt-api/comp/choice-item-filter/{erupt}/{field}`
根据当前表单数据动态过滤选项（级联下拉）。

**Body:** `Map<String, Object>`（当前表单数据）

**Response:** `List<VLModel>`

---

### `GET /erupt-api/comp/choice-trigger/{erupt}/{field}`
选项选中后触发联动更新。

**Params:** `val` — 选中的值

**Response:** `Map<String, Object>`

---

### `GET /erupt-api/comp/tags-item/{erupt}/{field}`
获取 TAGS 组件的预定义标签列表。

**Response:** `List<String>`

---

### `GET /erupt-api/comp/code-edit-hints/{erupt}/{field}`
获取代码编辑器的提示词列表。

**Response:** `List<String>`

---

## File — 文件上传/下载

### `POST /erupt-api/file/upload/{erupt}/{field}`
上传单个文件（ATTACHMENT / IMAGE 类型）。

**Form:** `file` (MultipartFile)

**Response:** `EruptApiModel`（data = 文件路径）

---

### `POST /erupt-api/file/uploads/{erupt}/{field}`
批量上传文件。

**Form:** `file[]` (MultipartFile[])

**Response:** `EruptApiModel`（data = 多个路径，分隔符 `|`）

---

### `POST /erupt-api/file/upload-html-editor/{erupt}/{field}`
为富文本编辑器（CKEditor/UEditor）上传图片。

**Form:** `upload` (MultipartFile)

**Response:** `Map`（兼容 CKEditor 格式）

---

### `GET /erupt-api/file/download-attachment/**`
下载已上传的附件。

**Path after `/download-attachment/`:** 文件相对路径

**Response:** 文件二进制流（`application/octet-stream`）

---

## 全局信息

### `GET /erupt-api/version`
获取框架版本号，无需鉴权。

**Response:** `String`（如 `"2.5.1"`）

---

## 鉴权说明

| verifyType | 说明 |
|-----------|------|
| `LOGIN`   | 登录即可访问 |
| `MENU`    | 需要对应菜单权限 |
| `ERUPT`   | 需要对应 Erupt 实体的操作权限 |

Token 默认放 Request Header（`token: xxx`），文件上传接口可通过 Query Param 传递（`?_token=xxx`）。

写入接口（新增/修改/删除）标记 `highSafe=true`，在高安全模式下会做额外身份校验。
