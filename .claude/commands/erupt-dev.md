# Erupt 开发助手

你是 Erupt 低代码框架的专家。本项目使用 Java 注解驱动自动生成后台管理 UI，无需前端代码。

当用户提问或请求开发帮助时，聚焦在以下三个核心知识点，**忽略其他 API 和接口细节**。

---

## 一、`@Erupt` 注解（作用于类）

```java
@Erupt(
    name = "功能名称",                          // 必填，显示名称
    desc = "",                                  // 功能描述
    primaryKeyCol = "id",                       // 主键列名，默认 "id"
    authVerify = true,                          // 是否校验权限
    orderBy = "",                               // HQL 排序，如 "t.createTime desc"
    dataProxy = { MyDataProxy.class },          // 数据代理，实现 DataProxy<T>
    dataProxyParams = {},                       // 传给 dataProxy 的参数，DataProxyContext.get() 获取
    filter = { @Filter(...) },                  // 数据过滤条件
    power = @Power(...),                        // 操作权限配置
    rowOperation = { @RowOperation(...) },      // 自定义功能按钮
    drills = { @Drill(...) },                   // 数据钻取
    tree = @Tree(...),                          // 树视图配置
    linkTree = @LinkTree(field = "xxx"),        // 左树右表配置
    layout = @Layout(...),                      // 布局配置
    vis = { @Vis(...) },                        // 多视图扩展
    param = { @KV(key="k", value="v") },       // 自定义扩展参数
    extra = {}                                  // 额外注解扩展
)
```

---

### `@Filter` — 数据过滤

```java
@Filter(
    value = "t.status = 1 and t.deleted = false",  // HQL where 条件（t 代表当前实体）
    params = {},                                     // 可在 conditionHandler 中获取
    conditionHandler = MyFilterHandler.class         // 动态处理过滤条件（实现 FilterHandler）
)
```

---

### `@Power` — 操作权限

```java
@Power(
    add = true,           // 新增
    edit = true,          // 编辑
    delete = true,        // 删除
    query = true,         // 查询
    print = true,         // 打印
    viewDetails = true,   // 查看详情
    export = false,       // 导出 Excel
    importable = false,   // 导入 Excel
    powerHandler = MyPowerHandler.class  // 动态权限（实现 PowerHandler）
)
```

---

### `@RowOperation` — 自定义按钮

```java
@RowOperation(
    title = "按钮名",                             // 必填
    code = "",                                    // 按钮标识码
    tip = "",                                     // 功能提示（hover 显示）
    callHint = "erupt.operation.call_hint",       // 调用前确认提示，空则不提示
    icon = "fa fa-dot-circle-o",                  // Font Awesome 图标
    fold = false,                                 // 是否折叠显示（按钮过多时）
    mode = RowOperation.Mode.MULTI,               // 依赖行数据模式（见下）
    type = RowOperation.Type.ERUPT,               // 按钮类型（见下）
    ifExpr = "",                                  // JS 表达式控制显示/禁用，变量: item（当前行数据）
    ifExprBehavior = IfExprBehavior.DISABLE,      // ifExpr 结果控制 HIDE（隐藏）/ DISABLE（禁用）
    show = @ExprBool(...),                        // 动态控制按钮是否显示
    eruptClass = MyForm.class,                    // 点击弹出的表单类（void.class 表示无表单）
    operationParam = {},                          // 传给 operationHandler 的静态参数
    operationHandler = MyHandler.class,           // 后端处理器（实现 OperationHandler）
    tpl = @Tpl(path = "")                         // type=TPL 时使用，可用 rows 变量获取选中行
)
```

**`RowOperation.Mode` 枚举：**
- `SINGLE` — 依赖单行数据（选中一行才可点击）
- `MULTI` — 依赖多行数据（可选多行，也可单行）
- `MULTI_ONLY` — 仅依赖多行（屏蔽单行操作按钮）
- `BUTTON` — 不依赖行数据（工具栏按钮）

**`RowOperation.Type` 枚举：**
- `ERUPT` — 弹出 eruptClass 表单，operationHandler 处理逻辑
- `TPL` — 自定义模板渲染（用 tpl 配置）

---

### `@Drill` — 数据钻取

```java
@Drill(
    title = "查看订单",              // 必填，钻取按钮标题
    code = "",                       // 标识码
    icon = "fa fa-sitemap",          // Font Awesome 图标
    fold = false,                    // 是否折叠显示
    show = @ExprBool(...),           // 动态控制是否显示
    link = @Link(                    // 下钻目标配置（必填）
        column = "id",               // 当前实体的关联列
        linkErupt = OrderItem.class, // 目标 erupt 类
        joinColumn = "orderId",      // 目标类中关联当前实体的列名
        linkCondition = ""           // 目标查询附加条件（HQL）
    )
)
```

---

### `@Tree` — 树视图

```java
@Tree(
    id = "id",           // 树节点存储列（主键）
    label = "name",      // 树节点展示列
    pid = "parentId",    // 父级节点标识列（不配置则不启用树）
    expandLevel = 999,   // 默认展开层级数
    rootPid = @Expr(...) // 根节点 pid 特征（默认 null 为根）
)
```

---

### `@LinkTree` — 左树右表

```java
@LinkTree(
    field = "dept",        // 实体中关联树的字段名
    dependNode = false     // 表格数据是否必须依赖树节点（false = 无选择时显示所有）
)
```

---

### `@Layout` — 布局配置

```java
@Layout(
    formSize = Layout.FormSize.DEFAULT,          // DEFAULT（默认）/ FULL_LINE（整行表单）
    tableLeftFixed = 0,                          // 表格左侧固定列数
    tableRightFixed = 0,                         // 表格右侧固定列数
    pagingType = Layout.PagingType.BACKEND,      // 分页方式（见下）
    pageSize = 10,                               // 默认分页大小
    pageSizes = {10, 20, 30, 50, 100, 300, 500}, // 可选分页数
    refreshTime = -1,                            // 自动刷新间隔（毫秒，-1 不刷新）
    tableWidth = "",                             // 表格宽度
    tableOperatorWidth = ""                      // 操作列宽度
)
```

**`PagingType` 枚举：**
- `BACKEND` — 后端分页（默认）
- `FRONT` — 前端分页
- `NONE` — 不分页（最多显示 pageSizes 最大值 × 10 条）

---

### `@Vis` — 多视图扩展

```java
@Vis(
    title = "卡片视图",                         // 必填，视图标签名
    code = "",                                  // 标识码
    desc = "",                                  // 描述
    type = Vis.Type.CARD,                       // 视图类型（见下）
    fieldVisibility = FieldVisibility.EXCLUDE,  // INCLUDE（只显示 fields 中的字段）/ EXCLUDE（排除 fields 中的字段）
    fields = {"remark"},                        // 与 fieldVisibility 配合
    filter = { @Filter("...") },               // 该视图的独立数据过滤
    orderBy = { @Sort(field="createTime", direction=Direction.DESC) }, // 该视图排序
    show = @ExprBool(...),                      // 动态控制视图是否显示
    cardView = @CardView(...),                  // type=CARD 时配置
    ganttView = @GanttView(...),                // type=GANTT 时配置
    tplView = @Tpl(path="")                     // type=TPL 时配置
)
```

**`Vis.Type` 枚举：** `TABLE` `CARD` `GANTT` `BOARD` `TPL`

**`@CardView` — 卡片视图：**
```java
@CardView(
    coverField = "coverImage",           // 封面图字段名
    coverEffect = CardView.CoverEffect.CLIP  // FIT（适应）/ CLIP（剪裁，默认）
)
```

**`@GanttView` — 甘特图视图：**
```java
@GanttView(
    startDateField = "startDate",    // 开始时间字段（必填）
    endDateField = "endDate",        // 结束时间字段（必填）
    groupField = "",                 // 分组字段
    pidField = "",                   // 父级字段（层级甘特图）
    progressField = "",              // 进度字段（0-100）
    colorField = ""                  // 颜色字段（hex 值）
)
```

**`@BoardView` — 看板视图：**
```java
// 注意：@BoardView 通过 extra 挂载，需引入 erupt-extra 中的 BoardView 注解
// 在 @Vis 中暂无直接属性，通过 extra 方式扩展
```

---

### `@Tpl` — 模板配置（通用）

```java
@Tpl(
    path = "/tpl/my-page.html",          // 模板文件路径或路由地址（必填）
    enable = true,                        // 是否启用
    params = {},                          // 静态参数，可在 tplHandler 中获取
    tplHandler = MyTplHandler.class,      // 绑定模板数据（实现 TplHandler.bindTplData）
    engine = Tpl.Engine.FreeMarker,       // 模板引擎：Native/FreeMarker/Thymeleaf/Velocity/Beetl/Enjoy
    embedType = PageEmbedType.IFRAME,     // 嵌入方式：IFRAME 或微前端
    openWay = OpenWay.MODAL,             // 弹出方式：MODAL（模态框）/ DRAWER（抽屉）
    drawerPlacement = Placement.RIGHT,   // 抽屉方向：RIGHT/LEFT/TOP/BOTTOM
    width = "800px",                      // 弹出层宽度
    height = "600px"                      // 弹出层高度
)
```

---

## 二、`@EruptField` 注解（作用于字段）

```java
@EruptField(
    views = { @View(...) },   // 表格列配置（可多列）
    edit = @Edit(...),        // 表单编辑组件配置
    sort = 1000               // 显示顺序，数值越小越靠前
)
```

---

### `@View` — 表格列

```java
@View(
    title = "列标题",                     // 必填
    desc = "",                            // 描述
    column = "",                          // 修饰对象类型时指定列路径，如 "dept.name"
    type = ViewType.AUTO,                 // 显示类型（见 ViewType 枚举）
    width = "",                           // 列宽，如 "120px" / "10%"
    show = true,                          // 是否显示
    sortable = false,                     // 是否可点击列头排序
    export = true,                        // 是否包含在 Excel 导出
    className = "",                       // CSS 类名
    template = "value + '元'",            // JS 格式化表达式，变量：value（当前值）/ item（整行）
    tpl = @Tpl(path="", enable=false),    // 单元格弹出层模板，可用 row 变量
    ifRender = @ExprBool(...)             // 动态渲染条件
)
```

**ViewType 枚举：**

| 枚举值 | 描述 |
|--------|------|
| `AUTO` | 自动判断 |
| `TEXT` | 普通文本 |
| `SAFE_TEXT` | 安全文本（HTML 转义渲染） |
| `COLOR` | 颜色色块 |
| `IMAGE` | 图片 |
| `IMAGE_BASE64` | Base64 图片 |
| `HTML` | HTML 渲染 |
| `MOBILE_HTML` | 手机端方式展示 |
| `QR_CODE` | 二维码 |
| `LINK` | 链接 |
| `LINK_DIALOG` | 对话框打开链接 |
| `DOWNLOAD` | 下载 |
| `ATTACHMENT` | 附件（新标签页预览，支持 pdf/mp4/svg 等） |
| `ATTACHMENT_DIALOG` | 对话框展示附件 |
| `DATE` | 日期 |
| `DATE_TIME` | 日期时间 |
| `BOOLEAN` | 开关 |
| `NUMBER` | 数值 |
| `MAP` | 地图 |
| `CODE` | 代码 |
| `TAB_VIEW` | 展示一对多/多对多数据 |
| `MARKDOWN` | Markdown |

---

### `@Edit` — 表单组件

```java
@Edit(
    title = "字段标题",                           // 必填
    desc = "",                                    // 描述（表单项下方提示）
    notNull = false,                              // 是否必填
    show = true,                                  // 是否显示此表单项
    placeHolder = "",                             // 输入框占位提示
    type = EditType.AUTO,                         // 组件类型（见 EditType 枚举）
    readonly = @Readonly(...),                    // 只读配置
    search = @Search(...),                        // 是否作为查询条件
    orderBy = "",                                 // 修饰关联对象时的排序 HQL
    filter = { @Filter("...") },                 // 修饰关联对象时的数据过滤
    dynamic = @Dynamic(...),                      // 表单字段联动
    onchange = MyOnChange.class,                  // 值变化时触发（实现 OnChange）
    onchangeParams = {},                          // onchange 可获取的静态参数
    ifRender = @ExprBool(...),                    // 动态渲染条件
    // 各类型专属配置（仅对应 type 生效）：
    inputType = @InputType(...),
    numberType = @NumberType(...),
    sliderType = @SliderType(...),
    rateType = @RateType(...),
    dateType = @DateType(...),
    boolType = @BoolType(...),
    choiceType = @ChoiceType(...),
    multiChoiceType = @MultiChoiceType(...),
    tagsType = @TagsType(...),
    attachmentType = @AttachmentType(...),
    htmlEditorType = @HtmlEditorType(...),
    autoCompleteType = @AutoCompleteType(...),
    referenceTreeType = @ReferenceTreeType(...),
    referenceTableType = @ReferenceTableType(...),
    checkboxType = @CheckboxType(...),
    codeEditType = @CodeEditorType(language="java"),
    tplType = @Tpl(path="")
)
```

---

#### `@Search` — 查询条件配置

```java
@Search(
    value = true,      // 是否作为查询条件
    vague = false,     // 是否启用高级查询（范围/模糊）
    notNull = false    // 查询条件是否必填
)
```

---

#### `@Readonly` — 只读配置

```java
@Readonly(
    add = true,            // 新增时只读
    edit = true,           // 编辑时只读
    allowChange = true,    // 是否允许通过 API 修改
    params = {},           // 传给 exprHandler 的参数
    exprHandler = MyReadonlyHandler.class  // 动态控制只读（实现 Readonly.ReadonlyHandler）
)
```

---

#### `@Dynamic` — 字段联动

```java
@Dynamic(
    dependField = "type",      // 依赖的字段名（同一表单内）
    condition = "type == 'VIP'", // JS 表达式，变量为各字段名
    match = Dynamic.Ctrl.SHOW,   // 条件满足时的控制：SHOW/HIDE/NOTNULL/READONLY
    noMatch = Dynamic.Ctrl.HIDE  // 条件不满足时的控制
)
```

---

#### `@ExprBool` — 动态布尔表达式

```java
@ExprBool(
    value = true,                         // 静态布尔值
    params = {},                           // 传给 exprHandler 的参数
    exprHandler = MyExprHandler.class     // 动态计算（实现 ExprBool.ExprHandler）
)
```

---

#### `@VL` — 值标签对（用于下拉选项）

```java
@VL(
    value = "1",        // 存储值
    label = "启用",     // 显示标签
    color = "#52c41a",  // hex 颜色（可选）
    disable = false,    // 是否禁用该选项
    desc = "",          // 描述
    extra = ""          // 额外扩展
)
```

---

### Edit 各组件子注解详解

#### `@InputType` — 文本输入（type=INPUT）

```java
@InputType(
    length = 255,           // 最大输入长度
    type = "text",          // HTML input type，如 "text"/"password"/"email"
    fullSpan = false,       // 是否占整行
    regex = "",             // 正则校验表达式
    autoTrim = true,        // 是否自动 trim
    prefix = {},            // 前缀选项（@VL 数组）
    suffix = {}             // 后缀选项（@VL 数组）
)
```

#### `@NumberType` — 数字输入（type=NUMBER）

```java
@NumberType(
    max = Integer.MAX_VALUE,   // 最大值
    min = -Integer.MAX_VALUE   // 最小值
)
```

#### `@SliderType` — 滑块（type=SLIDER）

```java
@SliderType(
    min = 0,              // 最小值
    max = 100,            // 最大值（必填）
    step = 1,             // 步进长度
    markPoints = {},      // 刻度标记点，如 {0, 25, 50, 75, 100}
    dots = false          // 是否只能拖拽到刻度上
)
```

#### `@RateType` — 评分（type=RATE）

```java
@RateType(
    count = 5,            // 总星数
    allowHalf = false,    // 是否允许半选
    character = ""        // 自定义字符（默认星形）
)
```

#### `@DateType` — 日期（type=DATE）

```java
@DateType(
    type = DateType.Type.DATE,              // DATE/TIME/DATE_TIME/MONTH/WEEK/YEAR
    pickerMode = DateType.PickerMode.ALL    // ALL（任意）/ FUTURE（仅未来）/ HISTORY（仅历史）
)
```

#### `@BoolType` — 布尔开关（type=BOOLEAN）

```java
@BoolType(
    trueText = "是",     // 真值显示文本
    falseText = "否"     // 假值显示文本
)
```

#### `@ChoiceType` — 单选（type=CHOICE）

```java
@ChoiceType(
    type = ChoiceType.Type.SELECT,       // SELECT（下拉）/ RADIO（单选框）
    vl = {                               // 静态选项列表
        @VL(value="1", label="启用", color="#52c41a"),
        @VL(value="0", label="禁用", color="#ff4d4f")
    },
    fetchHandler = { MyChoiceFetchHandler.class }, // 动态获取选项（实现 ChoiceFetchHandler）
    fetchHandlerParams = {},             // 传给 fetchHandler 的参数
    anewFetch = false,                   // 编辑时是否重新获取选项
    dependField = "category"             // 联动字段名（级联下拉）
)
```

#### `@MultiChoiceType` — 多选（type=MULTI_CHOICE）

```java
@MultiChoiceType(
    type = MultiChoiceType.Type.CHECKBOX,  // SELECT（下拉多选）/ CHECKBOX（复选框）
    vl = { @VL(...) },                     // 静态选项
    fetchHandler = { MyHandler.class },    // 动态获取选项
    fetchHandlerParams = {},
    dependField = ""                       // 联动字段名
)
```

#### `@TagsType` — 标签选择器（type=TAGS）

```java
@TagsType(
    joinSeparator = "|",             // 多个标签存储分隔符
    allowExtension = true,           // 是否允许自定义新标签
    maxTagCount = 9999,              // 最大标签数
    tags = {"Java", "Python"},       // 静态可选标签列表
    fetchHandler = { MyTagsHandler.class }, // 动态获取标签（实现 TagsFetchHandler）
    fetchHandlerParams = {}
)
```

#### `@AttachmentType` — 附件上传（type=ATTACHMENT）

```java
@AttachmentType(
    type = AttachmentType.Type.BASE,   // BASE（任意文件）/ IMAGE（仅图片）
    size = -1,                         // 文件大小限制（KB，-1 不限制）
    path = "",                         // 独享存储路径
    fileTypes = {"jpg","png","pdf"},   // 允许的文件类型后缀
    maxLimit = 1,                      // 最大上传数量
    fileSeparator = "|",               // 多文件存储分隔符
    imageType = @AttachmentType.ImageType(  // type=IMAGE 时生效
        minWidth = 0,
        maxWidth = Integer.MAX_VALUE,
        minHeight = 0,
        maxHeight = Integer.MAX_VALUE
    )
)
```

#### `@HtmlEditorType` — 富文本（type=HTML_EDITOR）

```java
@HtmlEditorType(
    value = HtmlEditorType.Type.UEDITOR,  // CKEDITOR / UEDITOR
    path = ""                              // 独享存储路径
)
```

#### `@AutoCompleteType` — 自动完成（type=AUTO_COMPLETE）

```java
@AutoCompleteType(
    handler = MyAutoCompleteHandler.class, // 必填，实现 AutoCompleteHandler
    param = {},                            // 传给 handler 的参数
    triggerLength = 1                      // 触发联想的最小输入长度
)
```

#### `@CodeEditorType` — 代码编辑器（type=CODE_EDITOR）

```java
@CodeEditorType(
    language = "java",   // 语言，如 "java"/"javascript"/"sql"/"json"/"html"/"xml"/"text"
    height = 300         // 编辑器高度（px）
)
```

#### `@ReferenceTreeType` — 树引用（type=REFERENCE_TREE，多对一）

```java
@ReferenceTreeType(
    id = "id",               // 存储列（关联实体主键）
    label = "name",          // 展示列
    pid = "parentId",        // 父级节点标识列（树结构必填）
    rootPid = @Expr(...),    // 根节点 pid 特征
    expandLevel = 999,       // 默认展开层级
    dependField = "category",  // 依赖字段名（级联联动）
    dependColumn = "id"        // 依赖字段值对应的列名（dependField.value = this.dependColumn）
)
```

#### `@ReferenceTableType` — 表格引用（type=REFERENCE_TABLE，多对一）

```java
@ReferenceTableType(
    id = "id",               // 存储列（关联实体主键）
    label = "name",          // 展示列
    dependField = "",        // 依赖字段名（级联联动）
    dependColumn = "id"      // 依赖字段值对应的列名
)
```

#### `@CheckboxType` — 多选（type=CHECKBOX，多对多）

```java
@CheckboxType(
    id = "id",        // 存储列
    label = "name",   // 展示列
    remark = ""       // 描述列
)
```

---

## 三、`eruptDao.lambdaQuery` — 类型安全查询

注入 `EruptDao`，调用 `eruptDao.lambdaQuery(Entity.class)` 构建链式查询。

```java
@Autowired
private EruptDao eruptDao;

List<User> list = eruptDao.lambdaQuery(User.class)
    .eq(User::getStatus, 1)
    .like(User::getName, "张")
    .orderByDesc(User::getCreateTime)
    .list();
```

### 条件方法（每个均有 `(boolean condition, ...)` 版本用于动态拼接）

```java
.eq(User::getField, val)                       // field = val
.ne(User::getField, val)                       // field <> val
.gt(User::getField, val)                       // field > val
.lt(User::getField, val)                       // field < val
.ge(User::getField, val)                       // field >= val
.le(User::getField, val)                       // field <= val
.between(User::getField, val1, val2)           // field between val1 and val2
.notBetween(User::getField, val1, val2)
.in(User::getField, collection)                // field in (...)
.in(User::getField, val1, val2, ...)
.notIn(User::getField, collection)
.notIn(User::getField, val1, val2, ...)
.like(User::getField, val)                     // field like %val%（自动加 %）
.likeValue(User::getField, val)                // field like val（自定义通配符）
.isNull(User::getField)
.isNotNull(User::getField)
.addCondition("u.age > :age", Map.of("age", 18))  // 原生条件（防注入）
.addCondition("u.status = 1")                      // 无参原生条件
.addParam("key", value)                            // 追加命名参数
```

**条件版本示例：**
```java
.eq(StringUtils.isNotBlank(name), User::getName, name)  // 第一参数为 false 时跳过
```

### 排序

```java
.orderBy(User::getField)              // asc
.orderByAsc(User::getField)
.orderByDesc(User::getField)
.orderBy(condition, User::getField)   // 条件版本
```

### 关联查询（with）

```java
.with(User::getDept)    // 指定路径前缀（用于跨关联字段查询）
.with()                 // 清空 with 配置
```

### 投影（SELECT 指定列）

```java
String name  = query.oneSelect(User::getName);               // 单条单列
List<String> names = query.listSelect(User::getName);        // 多条单列

// 多列映射到 DTO（DTO 需有无参构造 + 对应字段）
List<UserDto> list = query.listSelect(UserDto.class, User::getName, User::getAge);
UserDto one  = query.oneSelect(UserDto.class, User::getName, User::getAge);

// 路径字符串方式
List<String> results = query.selectByPath(String.class, "u.name");
```

### 聚合

```java
long total   = query.count();
long cnt     = query.count(User::getField);
Object sum   = query.sum(User::getAmount);
Double avg   = query.avg(User::getScore);
Object min   = query.min(User::getAge);
Object max   = query.max(User::getAge);
```

### 分页

```java
SimplePage<User> page = eruptDao.lambdaQuery(User.class)
    .eq(User::getStatus, 1)
    .page(pageSize, pageOffset);  // page(limit, offset)
// page.getTotal() — 总数
// page.getList()  — 当前页数据
```

### 结果获取 & 其他

```java
.one()               // 单条，无结果返回 null
.list()              // 返回 List
.limit(10)           // 最多返回 10 条
.offset(20)          // 跳过前 20 条
.distinct()          // 去重
.delete()            // 删除匹配记录
.deleteAndFlush()    // 删除并立即 flush
```

---

## 完整示例

```java
@Erupt(
    name = "用户管理",
    orderBy = "EruptUser.createTime desc",
    power = @Power(importable = true, export = true),
    filter = @Filter("t.deleted = false"),
    layout = @Layout(pageSize = 20, tableLeftFixed = 1),
    rowOperation = @RowOperation(
        title = "重置密码",
        mode = RowOperation.Mode.SINGLE,
        callHint = "确认重置该用户密码？",
        operationHandler = ResetPwdHandler.class
    ),
    vis = @Vis(
        title = "卡片视图",
        type = Vis.Type.CARD,
        cardView = @CardView(coverField = "avatar")
    )
)
@Entity
@Table(name = "t_user")
public class EruptUser extends MetaModelUpdateVo {

    @EruptField(
        views = @View(title = "用户名"),
        edit = @Edit(
            title = "用户名",
            notNull = true,
            search = @Search(true),
            inputType = @InputType(length = 50)
        )
    )
    private String username;

    @EruptField(
        views = @View(title = "状态", type = ViewType.BOOLEAN),
        edit = @Edit(
            title = "状态",
            type = EditType.BOOLEAN,
            boolType = @BoolType(trueText = "启用", falseText = "禁用")
        )
    )
    private Boolean status;

    @EruptField(
        views = @View(title = "角色", column = "role.name"),
        edit = @Edit(
            title = "角色",
            type = EditType.REFERENCE_TABLE,
            referenceTableType = @ReferenceTableType(id = "id", label = "name")
        )
    )
    @ManyToOne
    private EruptRole role;

    @EruptField(
        views = @View(title = "类型"),
        edit = @Edit(
            title = "类型",
            type = EditType.CHOICE,
            choiceType = @ChoiceType(vl = {
                @VL(value = "admin", label = "管理员", color = "#f50"),
                @VL(value = "user",  label = "普通用户", color = "#2db7f5")
            })
        )
    )
    private String userType;
}
```

```java
// DataProxy 示例
@Service
public class UserDataProxy implements DataProxy<EruptUser> {

    @Autowired
    private EruptDao eruptDao;

    @Override
    public void beforeAdd(EruptUser user) {
        // 检查用户名是否重复
        Long count = eruptDao.lambdaQuery(EruptUser.class)
            .eq(EruptUser::getUsername, user.getUsername())
            .count();
        if (count > 0) throw new EruptApiErrorTip("用户名已存在");
    }

    @Override
    public String searchCondition(String condition) {
        // 追加租户过滤
        return "t.tenantId = " + TenantContext.get();
    }
}
```
