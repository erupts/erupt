# erupt-data-feishu

Feishu Bitable (多维表格) data source for Erupt. Bind a `@Erupt` model to a Bitable table and Erupt manages records against the Feishu open platform — list / find / add / edit / delete all go through the official REST API.

Suited to teams that maintain business data in Feishu Bitable and want a permissioned, integrated admin view alongside their JPA/Mongo models.

## Configuration

Credentials live in Spring configuration (never in annotations):

```yaml
erupt:
  feishu:
    app-id: cli_xxx
    app-secret: xxx
    # For Lark (international), override:
    # base-url: https://open.larksuite.com
```

## Annotation

`@EruptFeishu`

| Attribute   | Default | Description                                          |
|-------------|---------|------------------------------------------------------|
| `baseToken` | —       | Bitable base identifier (`app_token`), e.g. `bascnXXXXXXXX` |
| `tableId`   | —       | Table identifier within the base, e.g. `tblXXXXXXXX`  |

Both values are visible in the URL / share link of the Bitable in the Feishu web client.

## Example

```java
@Getter
@Setter
@Erupt(name = "Product Backlog", primaryKeyCol = "recordId")
@EruptFeishu(baseToken = "bascnABCDEFG", tableId = "tblHIJKLMN")
@EruptDataProcessor(EruptFeishuDataService.DATA_PROCESSOR)
public class BacklogItem {

    @EruptField(views = @View(title = "Record ID"))
    private String recordId;

    @EruptField(
        views = @View(title = "Title"),
        edit = @Edit(title = "Title", notNull = true, search = @Search(vague = true))
    )
    private String title;

    @EruptField(
        views = @View(title = "Priority"),
        edit = @Edit(title = "Priority")
    )
    private String priority;

    @EruptField(
        views = @View(title = "Owner"),
        edit = @Edit(title = "Owner")
    )
    private String owner;

    @EruptField(
        views = @View(title = "Due"),
        edit = @Edit(title = "Due", type = EditType.DATE)
    )
    private Date due;
}
```

Field names on the model must match Bitable column names (case-sensitive as displayed in Feishu).

## Operations

- **List**: cursor-paged fetch of the whole table, filtered / sorted / paged in memory (LOCAL mode).
- **Find by id**: `GET /bitable/v1/apps/{baseToken}/tables/{tableId}/records/{recordId}`.
- **Add**: `POST .../records`; Feishu assigns the `recordId`.
- **Edit**: `PUT .../records/{recordId}`.
- **Delete**: `DELETE .../records/{recordId}`.

Tenant access tokens are acquired and refreshed automatically.

## Gotchas

- The primary key field maps to Bitable's `record_id` and is populated by Feishu on add — leave it empty in the form.
- LOCAL query mode fetches the full table; suited to config / dictionary scale data (hundreds to low thousands of rows), not to million-row Bitables.
- Feishu column types (select, multi-select, person, attachment) come back as their raw JSON structures; model them as `String` / `List<String>` and post-process in a `DataProxy` if you need typed access.
