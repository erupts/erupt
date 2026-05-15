# Erupt TPL 嵌入交互页面开发

用于在 Erupt 管理后台内嵌自定义 HTML 页面，可结合 Erupt REST API 实现完整的交互功能。

---

## 触发方式

`@Tpl` 可用于以下四个位置：

| 位置 | 注解写法 | 渲染 URL |
|-----|---------|---------|
| 行操作弹窗 | `@RowOperation(type=Type.TPL, tpl=@Tpl(...))` | `/erupt-api/tpl/operation-tpl/{erupt}/{code}?ids=...` |
| 表单字段 | `@Edit(type=EditType.TPL, tplType=@Tpl(...))` | `/erupt-api/tpl/html-field/{erupt}/{field}` |
| 多视图 | `@Vis(type=Vis.Type.TPL, tplView=@Tpl(...))` | `/erupt-api/tpl/vis-tpl/{erupt}/{code}` |
| 表格单元格查看 | `@View(tpl=@Tpl(...))` | `/erupt-api/tpl/view-tpl/{erupt}/{field}/{id}` |
| 独立菜单页 | `@TplAction` 注解在方法上（配合 `@EruptTpl` 类） | `/erupt-api/tpl/{action-name}` |

---

## @Tpl 注解参数速查

```java
@Tpl(
    path      = "/tpl/my-page.html",       // 模板文件路径（相对 resources/）
    engine    = Tpl.Engine.Native,          // 见下方引擎列表
    openWay   = OpenWay.MODAL,             // MODAL（模态框）/ DRAWER（抽屉）
    width     = "900px",
    height    = "600px",
    tplHandler = MyHandler.class,          // 可选，服务端数据注入
    params    = {"key1=val1"}              // 传给 tplHandler 的静态参数
)
```

**模板引擎：**

| Engine | 变量语法 | 适用场景 |
|--------|---------|---------|
| `Native` | 无服务端变量 | 纯前端页面，全靠 JS 调 API |
| `FreeMarker` | `${varName}`、`<#list>`  | 需要服务端预渲染数据 |
| `Thymeleaf` | `th:text="${varName}"` | Spring 生态熟悉者 |
| `Velocity` | `$varName`、`#foreach` | 简单场景 |

---

## 模板自动注入变量

无需 TplHandler，以下变量在所有引擎中自动可用：

| 变量名 | 类型 | 说明 |
|-------|------|------|
| `base` | String | 应用根路径（`request.contextPath`），用于拼接静态资源 URL |
| `request` | HttpServletRequest | HTTP 请求对象 |
| `rows` | `List<Map>` | 行操作选中的多行数据（行操作场景） |
| `row` | `Map` | 行操作选中的单行数据（单行场景） |

---

## TplHandler — 服务端数据注入

```java
// 在 @Tpl 注解中指定：tplHandler = MyTplHandler.class
@Component
public class MyTplHandler implements Tpl.TplHandler {
    @Resource
    private MyService myService;

    @Override
    public void bindTplData(Map<String, Object> binding, String[] params) {
        // binding 中已有 rows/row 等自动注入变量
        // params 来自 @Tpl(params={"key=val"})
        binding.put("chartData", myService.getChartData());
        binding.put("config", params[0]);
    }
}
```

在 FreeMarker 模板中使用：`${chartData}`

---

## 可用前端 UI 框架

引入路径均以 `${base}/` 开头（或直接用相对路径）。

### Element Plus（Vue 3，推荐）
```html
<link href="${base}/element-plus/element-plus.min.css" rel="stylesheet">
<script src="${base}/element-plus/vue.min.js"></script>
<script src="${base}/element-plus/element-plus.min.js"></script>
<script src="${base}/element-plus/axios.min.js"></script>
```

### Ant Design Vue（Vue 2）
```html
<link href="${base}/ant-design/antd.min.css" rel="stylesheet">
<script src="${base}/ant-design/vue.min.js"></script>
<script src="${base}/ant-design/antd.min.js"></script>
<script src="${base}/ant-design/axios.min.js"></script>
```

### Element UI（Vue 2）
```html
<link href="${base}/element/element.min.css" rel="stylesheet">
<script src="${base}/element/vue.min.js"></script>
<script src="${base}/element/element.min.js"></script>
<script src="${base}/element/axios.min.js"></script>
```

> 使用哪套框架需要在 pom.xml 中引入对应的 erupt-tpl 子模块：
> `erupt-tpl-ant-design` / `erupt-tpl-element-plus` / `erupt-tpl-element-ui`

---

## Token 获取（页面内调用 API）

嵌入页面 URL 中自动携带 `_token` 参数，直接读取：

```javascript
const token = new URLSearchParams(location.search).get('_token');
```

所有 API 调用将此 token 放入 Header：

```javascript
axios.defaults.headers.common['token'] = token;
// 或每次请求单独带
axios.get('/erupt-api/...', { headers: { token } });
```

---

## 完整示例：给定模型名生成数据查询页面

**场景**：为 `Order` 模型生成一个嵌入页面，展示分页表格 + 条件搜索。

### 1. Java 注解（行操作或独立 Vis 视图）

```java
@Erupt(
    name = "订单管理",
    vis = @Vis(
        title = "自定义视图",
        type = Vis.Type.TPL,
        tplView = @Tpl(path = "/tpl/order-view.html", engine = Tpl.Engine.Native,
                       width = "100%", height = "100%")
    )
)
@Entity
public class Order { ... }
```

### 2. 模板文件 `/src/main/resources/tpl/order-view.html`

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <link href="element-plus/element-plus.min.css" rel="stylesheet">
</head>
<body>
<div id="app" style="padding:16px">
  <!-- 搜索栏 -->
  <el-input v-model="keyword" placeholder="搜索..." style="width:200px;margin-bottom:12px"
            @keyup.enter="load(1)" clearable @clear="load(1)"/>
  <el-button type="primary" @click="load(1)">查询</el-button>

  <!-- 数据表格 -->
  <el-table :data="list" stripe border style="width:100%;margin-top:12px">
    <el-table-column prop="orderNo" label="订单号" />
    <el-table-column prop="amount"  label="金额" />
    <el-table-column prop="status"  label="状态" />
  </el-table>

  <!-- 分页 -->
  <el-pagination style="margin-top:12px"
    :total="total" :page-size="pageSize" :current-page="pageIndex"
    layout="total, prev, pager, next"
    @current-change="load" />
</div>

<script src="element-plus/vue.min.js"></script>
<script src="element-plus/element-plus.min.js"></script>
<script src="element-plus/axios.min.js"></script>
<script>
const token = new URLSearchParams(location.search).get('_token');
axios.defaults.headers.common['token'] = token;

Vue.createApp({
  data: () => ({ list: [], total: 0, pageIndex: 1, pageSize: 20, keyword: '' }),
  mounted() { this.load(1); },
  methods: {
    async load(page) {
      this.pageIndex = page;
      // {erupt} = 类名 Order，condition.key = Java 字段名
      const { data } = await axios.post('/erupt-api/data/table/Order', {
        pageIndex: this.pageIndex,
        pageSize:  this.pageSize,
        condition: this.keyword
          ? [{ key: 'orderNo', value: this.keyword, conditionType: 'LIKE' }]
          : []
      });
      this.list  = data.list;
      this.total = data.total;
    }
  }
}).use(ElementPlus).mount('#app');
</script>
</body>
</html>
```

---

## 生成交互页面的四步流程

遇到"给定模型名，生成交互页面"的需求时，按此流程：

1. **确认模型字段** — 先读取/询问 `@EruptField` 字段名（不是列名），作为表格列 `prop` 和 condition `key`
2. **选择触发方式** — 独立菜单用 `@EruptTpl + @TplAction`；附属在某个模型用 `@Vis(type=TPL)` 或 `@RowOperation(type=TPL)`
3. **选择 UI 框架** — 默认推荐 Element Plus（Vue 3），确认 pom 中有对应依赖
4. **生成模板** — token 从 `_token` URL 参数取；API 路径用 `{erupt}` = 类简名；字段键用 Java 字段名

---

## conditionType 速查

| 值 | 含义 |
|----|------|
| `EQ` | 等于 |
| `LIKE` | 模糊匹配 |
| `GT` / `LT` | 大于 / 小于 |
| `GTE` / `LTE` | 大于等于 / 小于等于 |
| `IN` | IN 查询（value 为数组） |
| `NOT_NULL` | 非空 |
