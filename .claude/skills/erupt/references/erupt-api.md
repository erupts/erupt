# Erupt Core REST API 参考

所有接口前缀 `/erupt-api`。

## 全局鉴权规则

**除以下接口外，所有接口均必须携带 Token，否则返回 401/403：**

| 无需 Token 的接口 | 说明 |
|-----------------|------|
| `GET /erupt-api/version` | 版本号 |
| `GET /erupt-api/login` | 登录获取 Token |
| `GET /erupt-api/code-img` | 验证码图片 |
| `GET /erupt-api/open-api/create-token` | Open API 获取 Token |

Token 传递方式（按优先级）：
1. HTTP Header：`token: <token值>`
2. URL 参数：`?_token=<token值>`（文件上传接口必须使用此方式）

### Token 的两种来源

| 场景 | 来源 | 获取方式 |
|-----|------|---------|
| **服务端/外部系统调用** | Open API | 用 `appid` + `secret` 调用 `/erupt-api/open-api/create-token` 获取 |
| **嵌入页面（TPL）内调用** | 页面上下文 | Erupt 在渲染嵌入页面时会将当前登录用户的 Token 以 `_token` 参数注入到页面 URL 中，直接从 URL 读取即可 |

**嵌入页面获取 Token 示例（JavaScript）：**
```javascript
const token = new URLSearchParams(location.search).get('_token');
// 之后用于 API 调用
fetch('/erupt-api/data/table/MyEntity', {
  method: 'POST',
  headers: { 'token': token, 'Content-Type': 'application/json' },
  body: JSON.stringify({ pageIndex: 1, pageSize: 20 })
});
```

> 嵌入页面中获取的 Token 即当前登录用户的 Token，调用 API 时权限与该用户完全一致，无需额外鉴权配置。

### 具体模型接口的访问控制

访问 `{erupt}` 相关接口时，框架还会做以下两层权限校验：

| 权限层 | 规则 |
|-------|------|
| **超管绕过** | 超级管理员账号拥有所有 Erupt 的全部操作权限，不受菜单和 `@Power` 限制 |
| **普通用户** | 必须在菜单管理中为该用户/角色分配了对应 Erupt 的访问菜单，且操作类型（新增/编辑/删除/导出等）须在 `@Power` 中开启 |

> 即使接口路径和参数完全正确，普通用户未被授权的 Erupt 模型同样会返回权限错误。Open API 应用继承所绑定 `eruptUser` 的权限，绑定超管则拥有全部权限。

---

## 反射约束（核心规则）

Erupt 的所有接口参数均通过 Java 反射解析，**不接受数据库列名、显示标题或任何自定义字符串**。

### 路径参数

| 路径参数 | 反射来源 | 示例 |
|---------|---------|------|
| `{erupt}` | `Class.getSimpleName()` — `@Erupt` 所标注类的简单类名 | `EruptUser`、`Order` |
| `{field}` / `{fieldName}` | Java 字段名 — `@EruptField` 所在字段的声明名 | `userType`、`dept` |
| `{tabName}` | Java 字段名 — `TAB_TABLE_ADD` / `TAB_TABLE_REFER` / `TAB_TREE` 类型字段名 | `orderItems` |
| `{code}` | `@RowOperation.code` 显式声明值 | `"resetPwd"` |

### 请求体与查询参数的字段键

反射约束**同样适用于请求体内部的字段键**，不仅限于路径：

| 位置 | 规则 |
|-----|------|
| Modify 接口 Body 的 JSON key | Java 字段名（非数据库列名） |
| 关联对象用 `.` 分隔 | `dept.id`（不是 `dept_id`） |
| `TableQuery.condition[].key` | Java 字段名；跨关联对象用 `.`，如 `dept.name` |
| `TableQuery.sort[].prop` | Java 字段名 |
| 响应 `Map<String,Object>` 的 key | Java 字段名（读取时同规则） |

### 从 EruptBuildModel 提取合法值

调用 `/erupt-api/build/{erupt}` 后可从响应中提取所有合法标识：

```
EruptBuildModel
  └── eruptModel.eruptFieldModels[]
        └── fieldName          ← {field} / {fieldName} / body key / condition.key / sort.prop
  └── tabErupts{}              ← key = {tabName}
  └── operationErupts{}        ← key = {code}（对应 @RowOperation.code）
  └── combineErupts{}          ← key = COMBINE 类型字段名
```

### 正误对比

```
# ❌ 用了数据库下划线列名
POST /erupt-api/data/modify/EruptUser
body: { "user_name": "tom", "dept_id": 1 }

# ✅ 用 Java 字段名，关联对象用对象引用
POST /erupt-api/data/modify/EruptUser
body: { "username": "tom", "dept": { "id": 1 } }

# ❌ 用了 @View.title 作路径参数
GET /erupt-api/comp/choice-item/EruptUser/用户类型

# ✅ 用 Java 字段名
GET /erupt-api/comp/choice-item/EruptUser/userType

# ❌ condition.key 用数据库列名
condition: [{ "key": "dept_id", "value": "2" }]

# ✅ 跨关联用点分隔
condition: [{ "key": "dept.id", "value": "2", "conditionType": "EQ" }]
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

## 鉴权体系

### Token 传递方式

所有需要鉴权的接口从请求中按以下优先级读取 Token（常量来自 `EruptReqHeaderConst`）：

| 方式 | 示例 | 适用场景 |
|-----|------|---------|
| HTTP Header `token` | `token: xxxxxxxx` | 推荐，普通 API 调用 |
| URL Query `_token` | `?_token=xxxxxxxx` | 文件上传/富文本图片上传（必须用此方式） |

---

### 方式一：普通用户登录（交互式）

**`GET /erupt-api/login`**

| 参数 | 必填 | 说明 |
|-----|------|------|
| `account` | ✓ | 用户账号 |
| `pwd` | ✓ | 密码，默认需加密：`md5(md5(pwd) + 当月日期 + account)` |
| `verifyCode` | — | 验证码（开启时必填） |
| `verifyCodeMark` | — | 验证码标识 |

> 密码加密可通过 `erupt-app.pwdTransferEncrypt=false` 关闭（不推荐生产环境）

**Response: `LoginModel`**
```json
{
  "pass": true,
  "token": "xxxxxxxxxxxxxxxx",
  "expire": "2026-05-15T10:45:00",
  "reason": "",
  "resetPwd": false,
  "useVerifyCode": false
}
```

- Token 有效期默认 **100 分钟**（`erupt.upms.expireTimeByLogin`，可配）
- 无刷新机制，过期后重新登录

---

### 方式二：Open API（外部系统对接，推荐）

适用于服务端程序调用，免去密码加密问题。

#### 前置配置

在管理后台 `Open API 管理` 中创建应用，系统自动生成：
- `appid` — 以 `es` 开头的 16 位字母数字
- `secret` — 24 位大写字母数字（可刷新，刷新后旧 secret 立即失效）
- `expire` — token 有效期（分钟，默认 3600）
- 绑定 `eruptUser` — 调用时继承该用户的菜单/数据权限

#### 获取 Token

**`GET /erupt-api/open-api/create-token`**

| 参数 | 必填 | 说明 |
|-----|------|------|
| `appid` | ✓ | 应用 ID |
| `secret` | ✓ | 应用密钥 |

**Response: `OpenApiTokenVo`**
```json
{
  "token": "ERxxxxxxxxxxxxxxxxxxxxxxxx",
  "expireTime": "2026-05-15T10:45:00"
}
```

- Open API token 以 `ER` 开头，便于区分普通用户 token
- 同一 appid 同时只有一个有效 token，重复调用旧 token 自动失效
- 过期后重新调用此接口获取新 token

---

### 其他鉴权相关接口

| 接口 | 说明 |
|-----|------|
| `GET /erupt-api/logout` | 注销当前 token |
| `GET /erupt-api/userinfo` | 获取当前用户信息 |
| `GET /erupt-api/menu` | 获取当前用户有权限的菜单列表 |
| `GET /erupt-api/code-img?mark=xx` | 获取验证码图片（无需鉴权） |
| `GET /erupt-api/change-pwd?pwd=xx&newPwd=xx&newPwd2=xx` | 修改密码 |

---

### 接口权限级别

| verifyType | 说明 |
|-----------|------|
| `LOGIN`   | 登录即可访问 |
| `MENU`    | 需要对应菜单权限 |
| `ERUPT`   | 需要对应 Erupt 实体的操作权限 |

写入接口（新增/修改/删除）标记 `highSafe=true`，在高安全模式下做额外身份校验。
