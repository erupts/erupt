# Erupt 多语言（i18n）开发助手

你是 Erupt 框架多语言国际化的专家。本文档覆盖 i18n 的所有机制：CSV 文件格式、加载规则、翻译 API、注解用法，以及如何从 `@Erupt` / `@EruptField` 注解信息自动扫描并生成 i18n CSV。

---

## 一、工作原理

1. 启动时，框架扫描 classpath 中所有 `i18n/` 目录下的 `*.csv` 文件（支持 JAR 内和文件系统）。
2. CSV 第一行为 **header**，第一列固定为 `key`，其余列为语言标识（如 `zh-CN`、`en-US`）。
3. 每行后续列为对应语言的翻译值；`key` 列值同时作为默认兜底（未找到翻译时返回原 key）。
4. 请求时读取 HTTP Header `Lang` 字段确定语言，缺失则使用配置的 `erupt.default-locales`（默认 `zh-CN`）。
5. 在类上标注 `@EruptI18n` 后，该类的所有注解文本（`@Erupt(name=...)` / `@EruptField` 的 `title`/`tip` 等）会自动走翻译管道。

---

## 二、CSV 文件规范

### 文件位置

```
src/main/resources/i18n/<模块名>.i18n.csv
```

框架递归扫描 `i18n/` 目录，文件名可任意，扩展名必须为 `.csv`。

### 文件格式

```csv
key,zh-CN,zh-TW,en-US,fr-FR,ja-JP,ko-KR,ru-RU,es-ES,de-DE,pt-PT,id-ID,ar-SA
```

- **第一行**：header，第一列必须是 `key`，其余列为语言标识（大小写不敏感）。
- **后续行**：第一列为翻译键，后续列依次对应 header 中的语言翻译值。
- 编码：**UTF-8**（必须，框架硬编码为 UTF-8 读取）。
- 单元格含逗号时用双引号包裹：`"hello, world"`。
- 框架内置语言标识：`zh-CN` `zh-TW` `en-US` `fr-FR` `ja-JP` `ko-KR` `ru-RU` `es-ES` `de-DE` `pt-PT` `id-ID` `ar-SA`

### 示例

```csv
key,zh-CN,en-US,ja-JP
order.status,订单状态,Order Status,注文状態
order.status.pending,待支付,Pending payment,支払い待ち
order.status.paid,已支付,Paid,支払い済み
order.delete_confirm,确认删除该订单？,Confirm deleting this order?,この注文を削除しますか？
```

### 注意事项

- **多个 CSV 文件的内容会合并**，同一 key 后加载的文件会覆盖先加载的（即业务模块可覆盖框架内置翻译）。
- 某语言列缺失某个 key 的值时，该单元格留空即可，翻译调用会返回原始 key 字符串。
- 框架本身的内置翻译 key（如 `erupt.notnull`、`erupt.operation.call_hint`）可通过在业务 CSV 中定义同名 key 进行覆盖。

---

## 三、`application.yml` 配置

```yaml
erupt:
  default-locales: zh-CN   # 无 Lang 请求头时使用的语言，默认 zh-CN
```

---

## 四、`@EruptI18n` 注解

```java
@EruptI18n               // 标注在 @Erupt 类上，开启该实体的注解文本翻译
@EruptI18n(enable=true)  // enable 默认为 true；设为 false 可临时关闭
```

**作用范围：** 标注后，该类所有注解中的文本字段（`@Erupt(name=...)` / `@EruptField` 的 `title`、`tip`、`callHint` 等）都会被当作翻译 key 传入 `I18nTranslate.translate(key)` 查找对应语言的值。

**典型用法：** 将注解文本写成 key，在 CSV 中维护各语言翻译。

```java
@EruptI18n
@Erupt(
    name = "order.title",           // key，CSV 中提供翻译
    rowOperation = @RowOperation(
        title = "order.confirm",    // key
        callHint = "order.confirm.hint"
    )
)
@Entity
public class Order extends MetaModelUpdateVo {

    @EruptField(
        views = @View(title = "order.status"),   // key
        edit = @Edit(title = "order.status")
    )
    private String status;
}
```

对应 CSV：

```csv
key,zh-CN,en-US
order.title,订单管理,Order Management
order.confirm,确认订单,Confirm Order
order.confirm.hint,确认后将锁定订单，是否继续？,Order will be locked after confirmation. Continue?
order.status,订单状态,Order Status
```

---

## 五、翻译 API

### `I18nTranslate`（注入使用）

```java
@Resource
private I18nTranslate i18nTranslate;

// 根据当前请求的 Lang Header 自动选择语言
String text = i18nTranslate.translate("order.status");

// 指定语言
String text = i18nTranslate.translate("en-US", "order.status");

// 获取当前请求的语言标识（无效/未设置时返回 null）
String lang = i18nTranslate.getLang();
```

### `I18nTranslate.$translate`（静态方法，非 Web 线程可用）

```java
// 静态调用，从 Spring 上下文获取 I18nTranslate Bean
String text = I18nTranslate.$translate("order.status");
```

### `I18nRunner`（底层，通常不直接使用）

```java
// 获取所有已加载的语言标识列表
List<String> langs = I18nRunner.langs();
// 返回如：["zh-CN", "zh-TW", "en-US", ...]

// 直接按语言和 key 查询（无匹配时返回 key 本身）
String value = I18nRunner.getI18nValue("en-US", "order.status");
```

---

## 六、从注解自动扫描生成 i18n CSV

以下是一个工具类，扫描指定包下所有标注了 `@EruptI18n` 的类，提取注解中的文本，生成 CSV 骨架（key 列 + zh-CN 列，其余语言列留空待填写）。

```java
import org.reflections.Reflections;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 扫描 @EruptI18n 注解类，提取注解文本，生成 i18n CSV 骨架
 * 生成结果：key + zh-CN 两列，其余语言列需人工填写（或接入翻译API补全）
 */
public class EruptI18nScanner {

    // 目标语言列（按需增减）
    private static final String[] LANGS = {
        "zh-CN", "zh-TW", "en-US", "fr-FR", "ja-JP",
        "ko-KR", "ru-RU", "es-ES", "de-DE", "pt-PT", "id-ID", "ar-SA"
    };

    public static void main(String[] args) throws Exception {
        // 修改为业务包名
        String basePackage = "com.yourcompany.yourmodule";
        String outputPath = "src/main/resources/i18n/app.i18n.csv";

        Map<String, String> entries = scan(basePackage);
        writeCsv(outputPath, entries);
        System.out.println("已生成 " + entries.size() + " 条翻译条目 → " + outputPath);
    }

    /**
     * 扫描指定包下所有 @EruptI18n 类，收集注解文本
     * key = 注解中的原始字符串，value = zh-CN（即原始文本本身作为中文翻译兜底）
     */
    public static Map<String, String> scan(String basePackage) throws Exception {
        // 使用 LinkedHashMap 保持插入顺序
        Map<String, String> entries = new LinkedHashMap<>();

        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(EruptI18n.class);

        for (Class<?> clazz : classes) {
            EruptI18n i18nAnno = clazz.getAnnotation(EruptI18n.class);
            if (!i18nAnno.enable()) continue;

            Erupt erupt = clazz.getAnnotation(Erupt.class);
            if (erupt == null) continue;

            // @Erupt.name / desc
            addEntry(entries, erupt.name());
            addEntry(entries, erupt.desc());

            // @RowOperation
            for (RowOperation op : erupt.rowOperation()) {
                addEntry(entries, op.title());
                addEntry(entries, op.tip());
                addEntry(entries, op.callHint());
            }

            // @Drill
            for (Drill drill : erupt.drills()) {
                addEntry(entries, drill.title());
            }

            // @EruptField 字段
            for (Field field : getAllFields(clazz)) {
                EruptField ef = field.getAnnotation(EruptField.class);
                if (ef == null) continue;

                // @View titles
                for (View view : ef.views()) {
                    addEntry(entries, view.title());
                    addEntry(entries, view.desc());
                }

                // @Edit title / desc / placeHolder
                Edit edit = ef.edit();
                if (edit != null && !edit.title().isEmpty()) {
                    addEntry(entries, edit.title());
                    addEntry(entries, edit.desc());
                    addEntry(entries, edit.placeHolder());
                }
            }
        }

        return entries;
    }

    private static void addEntry(Map<String, String> entries, String text) {
        if (text != null && !text.isEmpty()) {
            // key 与 zh-CN 值相同（原始文本即中文兜底）
            entries.putIfAbsent(text, text);
        }
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * 将 entries 写入 CSV 文件
     * 格式：key,zh-CN,zh-TW,en-US,...（其他语言列留空）
     */
    public static void writeCsv(String outputPath, Map<String, String> entries) throws IOException {
        File file = new File(outputPath);
        file.getParentFile().mkdirs();

        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            // header 行
            writer.println("key," + String.join(",", LANGS));

            for (Map.Entry<String, String> entry : entries.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(escapeCsv(entry.getKey()));    // key 列
                sb.append(",").append(escapeCsv(entry.getValue()));  // zh-CN 列
                for (int i = 1; i < LANGS.length; i++) {
                    sb.append(",");  // 其余语言列留空
                }
                writer.println(sb);
            }
        }
    }

    /**
     * 合并模式：将新扫描到的 key 追加到已有 CSV，不覆盖已有翻译
     */
    public static void mergeCsv(String csvPath, Map<String, String> newEntries) throws IOException {
        // 读取现有 CSV
        Map<String, String[]> existing = new LinkedHashMap<>();
        List<String> header = new ArrayList<>();
        File file = new File(csvPath);

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                boolean first = true;
                while ((line = reader.readLine()) != null) {
                    String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (first) {
                        header.addAll(Arrays.asList(cols));
                        first = false;
                    } else if (cols.length > 0) {
                        existing.put(cols[0], cols);
                    }
                }
            }
        } else {
            header.add("key");
            header.addAll(Arrays.asList(LANGS));
        }

        // 追加新 key（已存在的跳过）
        for (Map.Entry<String, String> entry : newEntries.entrySet()) {
            if (!existing.containsKey(entry.getKey())) {
                String[] row = new String[header.size()];
                row[0] = entry.getKey();
                // zh-CN 列（header 中第二列）填入原始文本
                int zhIdx = header.indexOf("zh-CN");
                if (zhIdx >= 0) row[zhIdx] = entry.getValue();
                existing.put(entry.getKey(), row);
            }
        }

        // 写回
        file.getParentFile().mkdirs();
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.println(String.join(",", header));
            for (String[] row : existing.values()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < header.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(escapeCsv(row.length > i ? (row[i] == null ? "" : row[i]) : ""));
                }
                writer.println(sb);
            }
        }
        System.out.println("合并完成，当前共 " + existing.size() + " 条 → " + csvPath);
    }

    private static String escapeCsv(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
```

---

## 七、完整开发流程

```
1. 在 @Erupt 类上加 @EruptI18n

2. 注解文本改写为 key（如 name="order.title"）

3. 运行 EruptI18nScanner.main() 扫描生成 CSV 骨架
   → src/main/resources/i18n/app.i18n.csv

4. 打开 CSV，填写各语言列翻译（或对接翻译API自动补全）

5. 追加新 key 时，使用 mergeCsv() 合并，不破坏已有翻译

6. 运行验证：
   I18nRunner.getI18nValue("en-US", "order.title")  // → "Order Management"
   I18nTranslate.$translate("order.status")          // → 当前请求语言对应的翻译
```

---

## 八、框架内置翻译 Key 速查

| Key | zh-CN 含义 |
|-----|-----------|
| `erupt.notnull` | 必填 |
| `erupt.operation.call_hint` | 请确认是否执行此操作 |
| `erupt.select` | 请先选择 |
| `erupt.exec_success` | 执行成功 |
| `erupt.exec_select_data` | 执行该操作时请至少选中一条数据 |
| `erupt.upload_error` | 上传失败 |
| `erupt.upload_error.file_format` | 上传失败，不被允许的文件格式 |
| `erupt.upload_error.size` | 上传失败，文件大小不能超过 |
| `erupt.data.data_duplication` | 数据重复 |
| `erupt.data.delete_fail_may_be_associated_data` | 删除失败，请检查关联数据 |
| `erupt.attack.xss` | 检测到有恶意跨站脚本 |
| `erupt.incorrect_format` | 格式不正确 |
| `erupt.must.number` | 必须为数值 |
| `Y` / `N` | 是 / 否（BoolType 默认值） |

业务 CSV 中定义同名 key 可覆盖上述内置翻译。
