# EruptCube 维度与指标开发指南

## 核心注解总览

| 注解           | 作用域 | 说明                   |
|--------------|-----|----------------------|
| `@EruptCube` | 类   | 声明一个数据立方体，绑定 SQL 数据源 |
| `@Dimension` | 字段  | 声明维度（用于分组、过滤）        |
| `@Measure`   | 字段  | 声明指标（聚合计算）           |
| `@Parameter` | 字段  | 声明查询参数（运行时传入）        |

---

## @EruptCube

```java
@EruptCube(
        name = "显示名称",          // 必填
        sql = "SQL 或表名",        // 必填
        sqlType = SqlType.SUB_QUERY,  // 默认 SUB_QUERY，表名用 TABLE_NAME
        description = "",
        explores = {@Explore(code = "overview", name = "Overview")},  // 默认值
        tags = {},
        dataProxy = {}
)
```

| 属性            | 类型        | 默认值         | 说明                                 |
|---------------|-----------|-------------|------------------------------------|
| `name`        | String    | 必填          | UI 显示名称                            |
| `sql`         | String    | 必填          | 子查询 SQL 或表名                        |
| `sqlType`     | SqlType   | `SUB_QUERY` | `TABLE_NAME`=直接引用表，`SUB_QUERY`=子查询 |
| `description` | String    | `""`        | 描述                                 |
| `explores`    | Explore[] | overview    | 对外暴露的查询视图                          |
| `tags`        | String[]  | `{}`        | 分类标签                               |
| `dataProxy`   | Class[]   | `{}`        | 数据代理处理器                            |

**SqlType 选择原则：**

- 单表、无需 JOIN → `SqlType.TABLE_NAME`，`sql` 直接填表名
- 多表 JOIN 或有复杂 SELECT → `SqlType.SUB_QUERY`（默认），`sql` 填完整 SELECT 语句

---

## @Dimension（维度）

```java

@Dimension(
        title = "显示名",   // 必填
        sql = "列名",     // 默认取字段名，列名与字段名不一致时必填
        type = FieldType.AUTO,
        description = "",
        hidden = false,
        tags = {}
)
private String channel;
```

| 属性       | 类型        | 默认值     | 说明                    |
|----------|-----------|---------|-----------------------|
| `title`  | String    | 必填      | UI 显示名称               |
| `sql`    | String    | `""`    | 对应 SQL 列名；为空时使用字段名    |
| `type`   | FieldType | `AUTO`  | 数据类型，AUTO 按 Java 类型推断 |
| `hidden` | boolean   | `false` | 是否在 UI 中隐藏            |

**FieldType 推断规则（AUTO）：**

| Java 类型                                 | 推断结果     |
|-----------------------------------------|----------|
| Short / Integer / Long / Double / Float | `NUMBER` |
| Date / LocalDateTime / LocalDate        | `DATE`   |
| 其他（String、Boolean、枚举等）                  | `STRING` |

---

## @Measure（指标）

```java

@Measure(
        title = "显示名",     // 必填
        sql = "count(*)",  // 必填，聚合 SQL 表达式
        type = FieldType.AUTO,
        description = "",
        hidden = false,
        tags = {}
)
private Long count;
```

| 属性      | 类型        | 默认值    | 说明                                 |
|---------|-----------|--------|------------------------------------|
| `title` | String    | 必填     | UI 显示名称                            |
| `sql`   | String    | 必填     | 聚合函数表达式，如 `count(*)`、`sum(amount)` |
| `type`  | FieldType | `AUTO` | 数据类型                               |

**常用聚合表达式：**

```java
sql ="count(*)"                                          // 总行数
sql ="count(distinct user_id)"                           // 去重计数
sql ="sum(amount)"                                       // 求和
sql ="avg(duration)"                                     // 均值
sql ="max(total_time)"                                   // 最大值
sql ="min(price)"                                        // 最小值
sql ="sum(case when success then 1 else 0 end)"          // 条件计数（成功）
sql ="sum(case when not success then 1 else 0 end)"      // 条件计数（失败）
sql ="round(sum(amount) / count(*), 2)"                  // 复合表达式
```

---

## @Parameter（查询参数）

```java

@Parameter(
        title = "显示名",
        type = FieldType.STRING,  // 默认 STRING
        vl = {@VL(value = "1", label = "选项A")}  // type=STRING 时可配枚举
)
private String status;
```

---

## @Explore（查询视图）

```java
@EruptCube(
        explores = {
                @Explore(
                        code = "overview",       // 必填，唯一标识
                        name = "Overview",       // 必填，显示名
                        where = "status = 1",     // 可选，追加 WHERE 条件
                        parameters = {
                                @ExploreParameter(code = "type", value = "order")
                        },
                        joins = {}
                )
        }
)
```

---

## CubeProxy（数据代理）

```java
public class MyCubeProxy implements CubeProxy {
    // 查询前动态修改表达式
    @Override
    public String beforeQuery(String expr, Map<String, Object> context) {
        return expr;
    }

    // 查询后处理结果
    @Override
    public void afterQuery(List<CubeResultRow> result, Map<String, Object> context) {
    }
}
```

---

## 生成维度和指标的完整流程

### Step 1：分析 SQL，确定可用列

拿到 `@EruptCube` 的 `sql`，列出所有 SELECT 的列（含别名），作为可用的维度/指标候选。

**多表 JOIN 时的列名规则：**

- 若列有别名（`column AS alias`），`@Dimension(sql=...)` 填别名
- 若列无别名但来自子查询，`@Dimension(sql=...)` 填列名
- `EruptAIChatCube` 示例：`message.sender_type as senderType` → `@Dimension(sql="senderType")`（取别名，不是原列名）

### Step 2：按语义划分维度 vs 指标

| 划为维度                 | 划为指标                       |
|----------------------|----------------------------|
| 描述性字段：状态、渠道、用户、时间、名称 | 数值聚合：count、sum、avg、max、min |
| 可用于分组、筛选             | 不可单独出现，必须配合聚合函数            |

### Step 3：补充有业务价值的派生指标

在原始列之外，补充常见的聚合指标：

- `count(*)` → 总量
- `count(distinct xxx_id)` → 去重数量（如独立用户数）
- `sum(case when ... then 1 else 0 end)` → 条件计数（成功/失败分类）
- `avg(duration)` / `max(duration)` → 性能分析

### Step 4：Java 字段类型与列名匹配

| SQL 列类型              | Java 字段类型                      | FieldType            |
|----------------------|--------------------------------|----------------------|
| VARCHAR / ENUM       | String                         | AUTO（推断为 STRING）     |
| TINYINT(1) / BOOLEAN | Boolean                        | AUTO（推断为 STRING，注意！） |
| BIGINT / INT         | Long / Integer                 | AUTO（推断为 NUMBER）     |
| DATETIME / TIMESTAMP | Date / LocalDateTime,LocalDate | AUTO（推断为 STRING）     |
| 聚合结果 count           | Long                           | AUTO                 |
| 聚合结果 avg             | Double                         | AUTO                 |

---

## 完整示例

### 示例 1：多表 JOIN 子查询（EruptNoticeLogCube）

```java

@EruptCube(
        name = "Erupt Notice Log",
        sql = """
                select detail.status,
                       detail.success,
                       detail.create_time,
                       detail.channel,
                       detail.receive_user_id,
                       log.title,
                       scene.name
                from e_notice_log_detail detail
                       inner join e_notice_log log on detail.notice_log_id = log.id
                       inner join e_notice_scene scene on log.notice_scene_id = scene.id
                """
)
public class EruptNoticeLogCube {

    @Dimension(title = "Scene", sql = "name")
    private String name;

    @Dimension(title = "Channel", sql = "channel")
    private String channel;

    @Dimension(title = "Status", sql = "status")
    private String status;

    @Dimension(title = "Is Success", sql = "success")
    private Boolean success;

    @Dimension(title = "Notice Title", sql = "title")
    private String title;

    @Dimension(title = "Receive User", sql = "receive_user_id")
    private Long receiveUserId;

    @Dimension(title = "Create Time", sql = "create_time")
    private Date createTime;

    @Measure(title = "Count", sql = "count(*)")
    private Long count;

    @Measure(title = "Success Count", sql = "sum(case when success then 1 else 0 end)")
    private Long successCount;

    @Measure(title = "Fail Count", sql = "sum(case when not success then 1 else 0 end)")
    private Long failCount;

    @Measure(title = "Unique Receivers", sql = "count(distinct receive_user_id)")
    private Long uniqueReceivers;
}
```

### 示例 2：直接引用表（EruptLoginLog）

```java

@EruptCube(
        name = "Erupt Login Log",
        sql = "e_upms_login_log",
        sqlType = SqlType.TABLE_NAME
)
public class EruptLoginLog extends BaseModel {

    @Dimension(title = "User Name", sql = "user_name")
    private String userName;

    @Dimension(title = "Login Time", sql = "login_time")
    private Date loginTime;

    @Dimension(title = "IP Address", sql = "ip")
    private String ip;

    @Dimension(title = "Browser", sql = "browser")
    private String browser;

    @Transient
    @Measure(title = "Count", sql = "count(*)")
    private Long count;
}
```

### 示例 3：与 @Erupt 实体类共用（EruptOperateLog）

```java
@Entity
@Table(name = "e_upms_operate_log")
@Erupt(name = "Operation Log", ...)

@EruptCube(name = "Erupt Operate Log", sql = "e_upms_operate_log", sqlType = SqlType.TABLE_NAME)
public class EruptOperateLog extends BaseModel {

    // @Dimension 和 @EruptField 可以同时标注在同一字段上
    @Dimension(title = "Operate User", sql = "operate_user")
    @EruptField(views = @View(title = "Operator"), ...)
    private String operateUser;

    // 指标字段需加 @Transient，避免 JPA 映射
    @Transient
    @Measure(title = "Count", sql = "count(*)")
    private Long count;

    @Transient
    @Measure(title = "Max Request Duration", sql = "max(total_time)")
    private Long maxRequestDuration;
}
```

---

## 注意事项

1. **纯 Cube 类**（不继承 BaseModel、不带 @Entity）：字段无需 `@Transient`
2. **JPA 实体类**：指标字段必须加 `@Transient`，否则 JPA 会尝试映射该列
3. **列名 vs 字段名**：`@Dimension(sql=...)` 填的是 SQL 列名/别名，不是 Java 字段名
4. **Boolean 维度**：Java `Boolean` 类型在 AUTO 模式下被推断为 STRING，行为正确（按字符串分组）
