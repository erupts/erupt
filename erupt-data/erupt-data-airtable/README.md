# erupt-data-airtable

Airtable data source for Erupt. Bind a `@Erupt` model to an Airtable table and Erupt manages records against the Airtable Web API — list / find / add / edit / delete all go through `/v0/{baseId}/{table}`.

Suited to teams that maintain business data in Airtable and want a permissioned, integrated admin view alongside their JPA/Mongo models.

## Configuration

The token lives in Spring configuration (never in annotations):

```yaml
erupt:
  airtable:
    token: patXXXXXXXX.XXXXXXXX
    # base-url: https://api.airtable.com
```

Create a personal access token at airtable.com/create/tokens with the `data.records:read` and `data.records:write` scopes and access to the bound bases.

## Annotation

`@EruptAirtable`

| Attribute | Default | Description                                              |
|-----------|---------|----------------------------------------------------------|
| `baseId`  | —       | Base identifier, e.g. `appXXXXXXXXXXXXXX`                |
| `table`   | —       | Table identifier (`tblXXXXXXXXXXXXXX`) or display name    |

Both values are visible in the table URL in the Airtable web client.

## Example

```java
@Getter
@Setter
@Erupt(name = "Product Backlog", primaryKeyCol = "recordId")
@EruptAirtable(baseId = "appABCDEFGHIJKLMN", table = "Backlog")
@EruptDataProcessor(EruptAirtableDataService.DATA_PROCESSOR)
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

Field names on the model must match Airtable column names (case-sensitive as displayed in Airtable).

## Operations

- **List**: `GET /v0/{baseId}/{table}?pageSize=100`, offset-paged fetch of the whole table, filtered / sorted / paged in memory (LOCAL mode).
- **Add**: `POST /v0/{baseId}/{table}`; Airtable assigns the `recordId`.
- **Edit**: `PATCH /v0/{baseId}/{table}/{recordId}` (only the sent fields change).
- **Delete**: `DELETE /v0/{baseId}/{table}/{recordId}`.

Writes are sent with `typecast: true`, so a plain string is accepted by select / date / linked-record fields.

## Gotchas

- The primary key field maps to the Airtable record `id` and is populated by Airtable on add — leave it empty in the form.
- Airtable rate-limits to 5 requests per second per base; LOCAL mode issues one request per 100 rows.
- LOCAL query mode fetches the full table; suited to config / dictionary scale data (hundreds to low thousands of rows).
- Attachments and collaborators flatten to their `url` / `name`; linked records come back as a list of `recXXXX` ids. Model them as `String` / `List<String>` and post-process in a `DataProxy` if you need typed access.
