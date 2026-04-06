# Erupt LambdaQuery 类型安全查询助手

你是 Erupt 框架 `eruptDao.lambdaQuery` 的专家。聚焦类型安全的链式查询 API，**忽略其他接口和注解细节**。

---

## 基本用法

注入 `EruptDao`，调用 `lambdaQuery(Entity.class)` 构建链式查询：

```java
@Autowired
private EruptDao eruptDao;

List<User> list = eruptDao.lambdaQuery(User.class)
    .eq(User::getStatus, 1)
    .like(User::getName, "张")
    .orderByDesc(User::getCreateTime)
    .list();
```

---

## 条件方法

**每个方法都有 `(boolean condition, ...)` 版本**，第一个参数为 `false` 时自动跳过该条件，用于动态查询场景。

### 比较

```java
.eq(User::getField, val)            // field = val
.ne(User::getField, val)            // field <> val
.gt(User::getField, val)            // field > val
.lt(User::getField, val)            // field < val
.ge(User::getField, val)            // field >= val
.le(User::getField, val)            // field <= val
```

### 范围

```java
.between(User::getField, val1, val2)     // field between val1 and val2
.notBetween(User::getField, val1, val2)
```

### 集合

```java
.in(User::getField, collection)          // field in (...)
.in(User::getField, val1, val2, ...)     // 变参形式
.notIn(User::getField, collection)
.notIn(User::getField, val1, val2, ...)
```

### 模糊

```java
.like(User::getField, val)           // field like %val%（自动加 %）
.likeValue(User::getField, val)      // field like val（自己控制通配符，如 "张%"）
```

### NULL 检查

```java
.isNull(User::getField)
.isNotNull(User::getField)
```

### 原生条件（防 SQL 注入）

```java
// 推荐：用命名参数占位符防注入
.addCondition("u.age > :age and u.score < :score", Map.of("age", 18, "score", 100))

// 纯静态条件（确保无注入风险时使用）
.addCondition("u.status = 1")

// 单独追加参数
.addParam("tenantId", getCurrentTenantId())
```

### 条件版本示例（动态查询）

```java
eruptDao.lambdaQuery(User.class)
    .eq(StringUtils.isNotBlank(status), User::getStatus, status)
    .like(keyword != null, User::getName, keyword)
    .between(startTime != null && endTime != null, User::getCreateTime, startTime, endTime)
    .list();
```

---

## 排序

```java
.orderBy(User::getField)                    // asc（默认）
.orderByAsc(User::getField)                 // 同上
.orderByDesc(User::getField)                // desc
.orderBy(condition, User::getField)         // 条件版本 asc
.orderByDesc(condition, User::getField)     // 条件版本 desc
```

---

## 关联查询（with）

跨关联字段查询时，用 `with` 指定关联路径前缀：

```java
// 查询 User 的关联 Dept 字段
eruptDao.lambdaQuery(User.class)
    .with(User::getDept)
    .eq(Dept::getName, "技术部")   // 此时字段路径以 dept. 为前缀
    .list();

.with()   // 清空 with，恢复默认路径
```

---

## 结果获取

```java
.one()     // 单条，无结果返回 null（多条结果时抛异常）
.list()    // 返回 List<T>
```

---

## 分页

```java
SimplePage<User> page = eruptDao.lambdaQuery(User.class)
    .eq(User::getStatus, 1)
    .orderByDesc(User::getCreateTime)
    .page(pageSize, pageOffset);   // page(limit, offset)

page.getTotal();   // 总条数
page.getList();    // 当前页数据
```

---

## 限制与偏移

```java
.limit(10)     // 最多返回 10 条
.offset(20)    // 跳过前 20 条（与 limit 配合实现手动分页）
.distinct()    // 去重
```

---

## 投影（SELECT 指定列）

### 单列

```java
// 查单条单列
String name = eruptDao.lambdaQuery(User.class)
    .eq(User::getId, id)
    .oneSelect(User::getName);

// 查多条单列
List<String> names = eruptDao.lambdaQuery(User.class)
    .eq(User::getStatus, 1)
    .listSelect(User::getName);
```

### 多列映射到 DTO

DTO 需要：无参构造 + 与查询字段名匹配的字段。

```java
// 多条
List<UserDto> list = eruptDao.lambdaQuery(User.class)
    .eq(User::getStatus, 1)
    .listSelect(UserDto.class, User::getName, User::getAge, User::getDeptId);

// 单条
UserDto one = eruptDao.lambdaQuery(User.class)
    .eq(User::getId, id)
    .oneSelect(UserDto.class, User::getName, User::getAge);
```

### 路径字符串方式

```java
List<String> result = eruptDao.lambdaQuery(User.class)
    .selectByPath(String.class, "u.dept.name");
```

---

## 聚合

```java
long total   = eruptDao.lambdaQuery(User.class).eq(User::getStatus, 1).count();
long cnt     = eruptDao.lambdaQuery(User.class).count(User::getField);
Object sum   = eruptDao.lambdaQuery(Order.class).sum(Order::getAmount);
Double avg   = eruptDao.lambdaQuery(Score.class).avg(Score::getPoint);
Object min   = eruptDao.lambdaQuery(User.class).min(User::getAge);
Object max   = eruptDao.lambdaQuery(User.class).max(User::getAge);
```

---

## 删除

```java
// 删除匹配记录，返回删除条数
int count = eruptDao.lambdaQuery(User.class)
    .eq(User::getStatus, 0)
    .delete();

// 删除并立即 flush 到数据库
int count = eruptDao.lambdaQuery(User.class)
    .eq(User::getStatus, 0)
    .deleteAndFlush();
```

---

## 完整示例

```java
@Service
public class UserService {

    @Autowired
    private EruptDao eruptDao;

    // 动态条件分页查询
    public SimplePage<EruptUser> page(String name, Integer status, int pageSize, int pageOffset) {
        return eruptDao.lambdaQuery(EruptUser.class)
            .like(StringUtils.isNotBlank(name), EruptUser::getUsername, name)
            .eq(status != null, EruptUser::getStatus, status)
            .orderByDesc(EruptUser::getCreateTime)
            .page(pageSize, pageOffset);
    }

    // 查单个字段（用于重复性校验）
    public boolean existsByUsername(String username) {
        return eruptDao.lambdaQuery(EruptUser.class)
            .eq(EruptUser::getUsername, username)
            .count() > 0;
    }

    // 查多列映射 DTO
    public List<UserOptionDto> listOptions() {
        return eruptDao.lambdaQuery(EruptUser.class)
            .eq(EruptUser::getStatus, true)
            .orderBy(EruptUser::getUsername)
            .listSelect(UserOptionDto.class, EruptUser::getId, EruptUser::getUsername);
    }

    // 聚合统计
    public Object totalAmount(Long userId) {
        return eruptDao.lambdaQuery(Order.class)
            .eq(Order::getUserId, userId)
            .eq(Order::getStatus, "PAID")
            .sum(Order::getAmount);
    }
}
```
