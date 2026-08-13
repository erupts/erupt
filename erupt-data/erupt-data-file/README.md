# erupt-data-file

File-backed data source for Erupt. Bind a `@Erupt` model to a flat file — CSV, TSV, JSON, JSON Lines, YAML, INI, Properties, Markdown-with-front-matter, or XML — and Erupt manages CRUD by re-reading the file on every query and rewriting it on every write.

Suited to configuration / dictionary / small-data scenarios (feature flags, translations, sample data, blog posts) — not to high-volume storage.

YAML support requires SnakeYAML on the classpath (typically already present via Spring Boot).

## Annotation

`@EruptFile`

| Attribute | Default      | Description                                                        |
|-----------|--------------|--------------------------------------------------------------------|
| `value`   | —            | File path (absolute or relative to the working directory)          |
| `type`    | `AUTO`       | File format; `AUTO` infers from the extension                      |
| `single`  | `false`      | `true` for a single-record file (settings/config), `false` for a list |

### Supported formats

| Extension                | Mode         | Notes                                                              |
|--------------------------|--------------|--------------------------------------------------------------------|
| `.csv`                   | list         | First line is the header of field names                            |
| `.tsv`                   | list         | Tab-separated                                                      |
| `.json`                  | list/single  | Supports nested fields; `single = true` gives an object shape       |
| `.jsonl` / `.ndjson`     | list         | One JSON object per line                                            |
| `.yml` / `.yaml`         | list/single  | Requires SnakeYAML                                                  |
| `.properties`            | single       | Java properties, always a single record                             |
| `.ini`                   | single       | `[section]`-grouped; each section maps to a nested object field     |
| `.md` / `.markdown`      | single       | Front-matter + body; body maps to the `content` field               |
| `.xml`                   | list/single  | Root element wraps `<item>` rows; `single = true` for one record    |

## Example — CSV dictionary

```java
@Getter
@Setter
@Erupt(name = "Country", primaryKeyCol = "code")
@EruptFile("data/countries.csv")
@EruptDataProcessor(EruptFileDataService.DATA_PROCESSOR)
public class Country {

    @EruptField(views = @View(title = "Code"))
    private String code;

    @EruptField(
        views = @View(title = "Name"),
        edit = @Edit(title = "Name", search = @Search(vague = true))
    )
    private String name;

    @EruptField(views = @View(title = "Continent"), edit = @Edit(title = "Continent"))
    private String continent;
}
```

## Example — single-record settings file

```java
@Getter
@Setter
@Erupt(name = "App Settings", primaryKeyCol = "id")
@EruptFile(value = "config/app.yml", single = true)
@EruptDataProcessor(EruptFileDataService.DATA_PROCESSOR)
public class AppSettings {

    @EruptField(views = @View(title = "ID"))
    private String id;

    @EruptField(edit = @Edit(title = "Site Title"))
    private String siteTitle;

    @EruptField(edit = @Edit(title = "Support Email"))
    private String supportEmail;
}
```

## Operations

Full CRUD. On add without a primary key: numeric fields get `max + 1`, everything else gets a UUID string.

## Gotchas

- The file is read/written whole on every operation — do not point this at multi-megabyte data.
- Missing files start empty and are created on the first write; parent directories must already exist.
- Single-record models should carry a primary key value in the file so drill / row operations can address the row.
