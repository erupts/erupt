# erupt-data-notion

Notion database data source for Erupt. Bind a `@Erupt` model to a Notion database and Erupt manages pages against the Notion API — list / find / add / edit / delete (soft-delete via archive).

Suited to teams that use Notion databases for CRM entries, docs indexes, editorial calendars, or configuration and want a permissioned, integrated admin view alongside their JPA/Mongo models.

## Configuration

The integration token lives in Spring configuration (never in annotations):

```yaml
erupt:
  notion:
    token: secret_xxxxxxxxxxxxx
    version: "2022-06-28"        # default; override to pin a different API version
    # base-url: https://api.notion.com   # override for a proxy
```

Create an internal integration at https://www.notion.so/my-integrations, then share each target database with that integration.

## Annotation

`@EruptNotion`

| Attribute    | Default | Description                                                  |
|--------------|---------|--------------------------------------------------------------|
| `databaseId` | —       | Notion database id (32-char id, with or without dashes)      |

The database id is the segment of the database URL after your workspace slug.

## Example

```java
@Getter
@Setter
@Erupt(name = "Content Calendar", primaryKeyCol = "pageId")
@EruptNotion(databaseId = "abcdef0123456789abcdef0123456789")
@EruptDataProcessor(EruptNotionDataService.DATA_PROCESSOR)
public class ContentEntry {

    @EruptField(views = @View(title = "Page ID"))
    private String pageId;

    @EruptField(
        views = @View(title = "Title"),
        edit = @Edit(title = "Title", notNull = true, search = @Search(vague = true))
    )
    private String title;

    @EruptField(
        views = @View(title = "Status"),
        edit = @Edit(title = "Status")
    )
    private String status;

    @EruptField(
        views = @View(title = "Author"),
        edit = @Edit(title = "Author")
    )
    private String author;

    @EruptField(
        views = @View(title = "Publish On"),
        edit = @Edit(title = "Publish On", type = EditType.DATE)
    )
    private Date publishOn;
}
```

Field names on the model must match Notion property names (case-sensitive, as displayed in the database view).

## Operations

- **List**: `POST /v1/databases/{databaseId}/query`, cursor-paged fetch of the whole database, filtered / sorted / paged in memory (LOCAL mode).
- **Find by id**: `GET /v1/pages/{pageId}`.
- **Add**: `POST /v1/pages`; Notion assigns the `pageId`.
- **Edit**: `PATCH /v1/pages/{pageId}`.
- **Delete**: `PATCH /v1/pages/{pageId}` with `archived: true` — Notion's API has no hard delete.

## Gotchas

- The primary key field maps to the Notion page `id` and is populated by Notion on add — leave it empty in the form.
- Delete is a soft delete (archive). Archived pages disappear from list results but remain in Notion; unarchive from the Notion UI if needed.
- Notion property types (select, multi-select, relation, person, files) come back as their raw JSON structures; model them as `String` / `List<String>` and post-process in a `DataProxy` for typed access.
- LOCAL query mode fetches the whole database on each list; suited to config / editorial scale (hundreds to low thousands of pages), not to huge databases.
