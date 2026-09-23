# erupt-data-dingtalk

DingTalk Notable (钉钉多维表) data source for Erupt. Bind a `@Erupt` model to a Notable sheet and Erupt manages records against the DingTalk open platform — list / find / add / edit / delete all go through the official `/v1.0/notable` REST API.

Suited to teams that maintain business data in DingTalk Notable and want a permissioned, integrated admin view alongside their JPA/Mongo models.

## Configuration

Credentials live in Spring configuration (never in annotations). Client ID / Client Secret are what the DingTalk developer console shows for an enterprise-internal app (older consoles call them AppKey / AppSecret):

```yaml
erupt:
  dingtalk:
    client-id: dingxxxxxxxx
    client-secret: xxx
    # union id of the user API calls act on behalf of (required by Notable)
    operator-id: xxxxxxxx
    # base-url: https://api.dingtalk.com
```

The app needs the Notable permissions (`Notable.Data.Read` / `Notable.Data.Write` or equivalent) granted in the DingTalk developer console, and the operator must have access to the base.

## Annotation

`@EruptDingTalk`

| Attribute    | Default                      | Description                                        |
|--------------|------------------------------|----------------------------------------------------|
| `baseId`     | —                            | Notable base identifier, from the document URL     |
| `sheet`      | —                            | Sheet identifier or its display name               |
| `operatorId` | `erupt.dingtalk.operator-id` | Per-model override of the operator union id        |

## Example

```java
@Getter
@Setter
@Erupt(name = "Product Backlog", primaryKeyCol = "recordId")
@EruptDingTalk(baseId = "abc123", sheet = "Backlog")
@EruptDataProcessor(EruptDingTalkDataService.DATA_PROCESSOR)
public class BacklogItem {

    @EruptField(views = @View(title = "Record ID"))
    private String recordId;

    @EruptField(
        views = @View(title = "Title"),
        edit = @Edit(title = "Title", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String title;

    @EruptField(
        views = @View(title = "Priority"),
        edit = @Edit(title = "Priority")
    )
    private String priority;

    @EruptField(
        views = @View(title = "Due"),
        edit = @Edit(title = "Due", type = EditType.DATE)
    )
    private Date due;
}
```

Field names on the model must match Notable column names (case-sensitive as displayed in DingTalk).

## Operations

- **List**: `POST .../sheets/{sheet}/records/list`, cursor-paged fetch of the whole sheet, filtered / sorted / paged in memory (LOCAL mode).
- **Add**: `POST .../records` with `{ records: [{ fields }] }`; DingTalk assigns the `recordId`.
- **Edit**: `PUT .../records` with `{ records: [{ id, fields }] }`.
- **Delete**: `POST .../records/delete` with `{ recordIds }`.

Access tokens are acquired and refreshed automatically.

## Gotchas

- The primary key field maps to the Notable record `id` and is populated by DingTalk on add — leave it empty in the form.
- Every Notable call requires an operator union id; configure it globally or per model.
- LOCAL query mode fetches the full sheet; suited to config / dictionary scale data (hundreds to low thousands of rows).
- Column types such as select, multi-select, person and attachment come back as their raw JSON structures; model them as `String` / `List<String>` and post-process in a `DataProxy` if you need typed access.
