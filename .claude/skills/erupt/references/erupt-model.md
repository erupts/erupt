# Erupt 注解开发助手

你是 Erupt 低代码框架注解开发的专家。本项目使用 Java 注解驱动自动生成后台管理 UI，无需前端代码。

聚焦 `@Erupt` 和 `@EruptField` 两个核心注解及其所有子注解，**忽略其他 API 和接口细节**。

---

## `@Erupt` 注解（作用于类）

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
    formSize = Layout.FormSize.DEFAULT,           // DEFAULT（默认）/ FULL_LINE（整行表单）
    tableLeftFixed = 0,                           // 表格左侧固定列数
    tableRightFixed = 0,                          // 表格右侧固定列数
    pagingType = Layout.PagingType.BACKEND,       // 分页方式（见下）
    pageSize = 10,                                // 默认分页大小
    pageSizes = {10, 20, 30, 50, 100, 300, 500},  // 可选分页数
    refreshTime = -1,                             // 自动刷新间隔（毫秒，-1 不刷新）
    tableWidth = "",                              // 表格宽度
    tableOperatorWidth = ""                       // 操作列宽度
)
```

**`PagingType` 枚举：** `BACKEND`（后端分页）/ `FRONT`（前端分页）/ `NONE`（不分页）

---

### `@Vis` — 多视图扩展

```java
@Vis(
    title = "卡片视图",                         // 必填，视图标签名
    code = "",                                  // 标识码
    desc = "",                                  // 描述
    type = Vis.Type.CARD,                       // 视图类型：TABLE/CARD/GANTT/BOARD/TPL
    fieldVisibility = FieldVisibility.EXCLUDE,  // INCLUDE（只显示 fields）/ EXCLUDE（排除 fields）
    fields = {"remark"},                        // 与 fieldVisibility 配合使用
    filter = { @Filter("...") },               // 该视图独立数据过滤
    orderBy = { @Sort(field="createTime", direction=Direction.DESC) },
    show = @ExprBool(...),                      // 动态控制视图是否显示
    cardView = @CardView(...),                  // type=CARD 时配置
    ganttView = @GanttView(...),                // type=GANTT 时配置
    tplView = @Tpl(path="")                     // type=TPL 时配置
)
```

**`@CardView`：**
```java
@CardView(
    coverField = "coverImage",              // 封面图字段名
    coverEffect = CardView.CoverEffect.CLIP // FIT（适应）/ CLIP（剪裁，默认）
)
```

**`@GanttView`：**
```java
@GanttView(
    startDateField = "startDate",  // 开始时间字段（必填）
    endDateField = "endDate",      // 结束时间字段（必填）
    groupField = "",               // 分组字段
    pidField = "",                 // 父级字段（层级甘特图）
    progressField = "",            // 进度字段（0-100）
    colorField = ""                // 颜色字段（hex 值）
)
```

---

### `@Tpl` — 模板配置（通用）

```java
@Tpl(
    path = "/tpl/my-page.html",         // 模板文件路径或路由地址（必填）
    enable = true,                       // 是否启用
    params = {},                         // 静态参数，可在 tplHandler 中获取
    tplHandler = MyTplHandler.class,     // 绑定模板数据（实现 TplHandler.bindTplData）
    engine = Tpl.Engine.FreeMarker,      // Native/FreeMarker/Thymeleaf/Velocity/Beetl/Enjoy
    embedType = PageEmbedType.IFRAME,    // 嵌入方式：IFRAME 或微前端
    openWay = OpenWay.MODAL,            // MODAL（模态框）/ DRAWER（抽屉）
    drawerPlacement = Placement.RIGHT,  // RIGHT/LEFT/TOP/BOTTOM
    width = "800px",
    height = "600px"
)
```

---

## `@EruptField` 注解（作用于字段）

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
| `ATTACHMENT` | 附件（新标签页预览） |
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

**EditType 枚举（组件类型）：**

| 枚举值 | 描述 | 适用类型 |
|--------|------|---------|
| `AUTO` | 自动检测 | 任意 |
| `INPUT` | 文本输入框 | String、数字 |
| `NUMBER` | 数字输入框 | 数字 |
| `SLIDER` | 数字滑块 | 数字 |
| `COLOR` | 颜色选择器 | String |
| `RATE` | 评分 | 数字 |
| `DATE` | 日期选择器 | String、Date |
| `BOOLEAN` | 布尔开关 | boolean |
| `CHOICE` | 单选（下拉/单选框） | String、数字 |
| `MULTI_CHOICE` | 多选 | 对象 |
| `TAGS` | 标签选择器 | String、数字 |
| `AUTO_COMPLETE` | 自动完成 | String |
| `TEXTAREA` | 多行文本 | String |
| `HTML_EDITOR` | 富文本编辑器 | String |
| `CODE_EDITOR` | 代码编辑器 | String |
| `MARKDOWN` | Markdown 编辑器 | String |
| `ATTACHMENT` | 附件上传 | String |
| `MAP` | 地图 | String |
| `TPL` | 自定义 HTML 模板 | String |
| `DIVIDE` | 横向分割线 | — |
| `HIDDEN` | 隐藏字段 | 任意 |
| `EMPTY` | 占位空白 | — |
| `SIGNATURE` | 签名板 | String |
| `REFERENCE_TREE` | 树引用（多对一） | 对象 |
| `REFERENCE_TABLE` | 表格引用（多对一） | 对象 |
| `CHECKBOX` | 多选（多对多） | 对象集合 |
| `TAB_TREE` | 多选树（多对多） | 对象集合 |
| `TAB_TABLE_REFER` | 多选表格（多对多） | 对象集合 |
| `TAB_TABLE_ADD` | 表格添加（一对多） | 对象集合 |
| `COMBINE` | 表格合并（一对一） | 对象 |

---

### `@Edit` 通用子注解

#### `@Search` — 查询条件

```java
@Search(
    value = true,      // 是否作为查询条件
    notNull = false    // 查询条件是否必填
)
```

#### `@Readonly` — 只读

```java
@Readonly(
    add = true,            // 新增时只读
    edit = true,           // 编辑时只读
    allowChange = true,    // 是否允许通过 API 修改
    params = {},
    exprHandler = MyReadonlyHandler.class  // 动态控制（实现 Readonly.ReadonlyHandler）
)
```

#### `@Dynamic` — 字段联动

```java
@Dynamic(
    dependField = "type",          // 依赖的字段名（同一表单内）
    condition = "type == 'VIP'",   // JS 表达式，变量为各字段名
    match = Dynamic.Ctrl.SHOW,     // 满足时：SHOW/HIDE/NOTNULL/READONLY
    noMatch = Dynamic.Ctrl.HIDE    // 不满足时
)
```

#### `@ExprBool` — 动态布尔表达式

```java
@ExprBool(
    value = true,
    params = {},
    exprHandler = MyExprHandler.class  // 实现 ExprBool.ExprHandler
)
```

#### `@VL` — 值标签对（下拉选项）

```java
@VL(value = "1", label = "启用", color = "#52c41a", disable = false, desc = "", extra = "")
```

---

### `@Edit` 组件专属子注解

#### `@InputType`（type=INPUT）
```java
@InputType(length=255, type="text", fullSpan=false, regex="", autoTrim=true, prefix={}, suffix={})
```

#### `@NumberType`（type=NUMBER）
```java
@NumberType(max=Integer.MAX_VALUE, min=-Integer.MAX_VALUE)
```

#### `@SliderType`（type=SLIDER）
```java
@SliderType(min=0, max=100, step=1, markPoints={}, dots=false)
```

#### `@RateType`（type=RATE）
```java
@RateType(count=5, allowHalf=false, character="")
```

#### `@DateType`（type=DATE）
```java
@DateType(
    type = DateType.Type.DATE,           // DATE/TIME/DATE_TIME/MONTH/WEEK/YEAR
    pickerMode = DateType.PickerMode.ALL // ALL/FUTURE/HISTORY
)
```

#### `@BoolType`（type=BOOLEAN）
```java
@BoolType(trueText="是", falseText="否")
```

#### `@ChoiceType`（type=CHOICE）
```java
@ChoiceType(
    type = ChoiceType.Type.SELECT,              // SELECT（下拉）/ RADIO（单选框）
    vl = { @VL(value="1", label="启用", color="#52c41a") },
    fetchHandler = { MyChoiceFetchHandler.class },
    fetchHandlerParams = {},
    anewFetch = false,                          // 编辑时是否重新获取选项
    dependField = "category"                    // 联动字段名（级联下拉）
)
```

#### `@MultiChoiceType`（type=MULTI_CHOICE）

推荐两种存储方式：

**方式一：JSON 存储（推荐，简单场景）**
```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(length = AnnotationConst.CONFIG_LENGTH)
@EruptField(
        views = @View(title = "Roles"),
        edit = @Edit(title = "Roles", type = EditType.MULTI_CHOICE,
                multiChoiceType = @MultiChoiceType(vl = {
                        @VL(value = "ADMIN", label = "Admin"),
                        @VL(value = "USER", label = "User"),
                        @VL(value = "GUEST", label = "Guest")
                }))
)
private Set<String> roles;
```

**方式二：一对多中间表存储（需要关联查询的场景）**
```java
@ElementCollection(fetch = FetchType.EAGER)
// 创建中间表 multi_table，id 为当前表的主键，呈一对多关联关系
@CollectionTable(
    name = "multi_table",
    joinColumns = @JoinColumn(name = "id"),
    foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
)
@Column(name = "mid")
@EruptField(
    views = @View(title = "多选"),
    edit = @Edit(
        title = "多选",
        type = EditType.MULTI_CHOICE,
        multiChoiceType = @MultiChoiceType(
            vl = {
                @VL(value = "1", label = "A"),
                @VL(value = "2", label = "B"),
                @VL(value = "3", label = "C"),
            }
        )
    )
)
private Set<Integer> mid;
```

`@MultiChoiceType` 参数：
```java
@MultiChoiceType(
    type = MultiChoiceType.Type.CHECKBOX,  // SELECT（下拉多选）/ CHECKBOX（复选框）
    vl = { @VL(...) },
    fetchHandler = { MyHandler.class },
    fetchHandlerParams = {},
    dependField = ""
)
```

#### `@TagsType`（type=TAGS）
```java
@TagsType(
    joinSeparator="|", allowExtension=true, maxTagCount=9999,
    tags={"Java","Python"},
    fetchHandler={ MyTagsHandler.class }, fetchHandlerParams={}
)
```

#### `@AttachmentType`（type=ATTACHMENT）
```java
@AttachmentType(
    type = AttachmentType.Type.BASE,  // BASE（任意文件）/ IMAGE（仅图片）
    size = -1,                        // KB，-1 不限制
    path = "",
    fileTypes = {"jpg","png","pdf"},
    maxLimit = 1,
    fileSeparator = "|",
    imageType = @AttachmentType.ImageType(minWidth=0, maxWidth=MAX, minHeight=0, maxHeight=MAX)
)
```

#### `@HtmlEditorType`（type=HTML_EDITOR）
```java
@HtmlEditorType(value=HtmlEditorType.Type.UEDITOR, path="")  // CKEDITOR / UEDITOR
```

#### `@AutoCompleteType`（type=AUTO_COMPLETE）
```java
@AutoCompleteType(handler=MyAutoCompleteHandler.class, param={}, triggerLength=1)
```

#### `@CodeEditorType`（type=CODE_EDITOR）
```java
@CodeEditorType(language="java", height=300)
// language: "java"/"javascript"/"sql"/"json"/"html"/"xml"/"text"
```

#### `@ReferenceTreeType`（type=REFERENCE_TREE，多对一）
```java
@ReferenceTreeType(
    id="id", label="name", pid="parentId",
    rootPid=@Expr(...), expandLevel=999,
    dependField="category", dependColumn="id"
)
```

#### `@ReferenceTableType`（type=REFERENCE_TABLE，多对一）
```java
@ReferenceTableType(id="id", label="name", dependField="", dependColumn="id")
```

#### `@CheckboxType`（type=CHECKBOX，多对多）
```java
@CheckboxType(id="id", label="name", remark="")
```

---

## 完整示例

```java
@EruptI18n
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

    @EruptField(
        views = @View(title = "所属角色", column = "role.name"),
        edit = @Edit(
            title = "所属角色",
            type = EditType.REFERENCE_TABLE,
            referenceTableType = @ReferenceTableType(id = "id", label = "name")
        )
    )
    @ManyToOne
    private EruptRole role;
}
```
