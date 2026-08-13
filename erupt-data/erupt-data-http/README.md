# erupt-data-http

REST-backed data source for Erupt. Bind a `@Erupt` model to a JSON HTTP endpoint — Erupt drives list / add / edit / delete against your service without any DAO or Spring proxy on the client side.

Uses the JDK's built-in `HttpClient` — no extra runtime dependency beyond `erupt-core`.

## Expected endpoint shape

```
GET    {value}         → [ {...}, ... ]   or   { "total": n, "list": [ ... ] }
GET    {value}/{id}    → { ... }
POST   {value}         add
PUT    {value}/{id}    edit
DELETE {value}/{id}    delete
```

## Annotation

`@EruptHttp`

| Attribute   | Default | Description                                                                  |
|-------------|---------|------------------------------------------------------------------------------|
| `value`     | —       | Resource base URL                                                            |
| `headers`   | `{}`    | Extra request headers as `"Name: Value"` strings                             |
| `queryMode` | `LOCAL` | `LOCAL` fetches the full list and filters in memory; `REMOTE` delegates paging |
| `timeout`   | `10`    | Request timeout in seconds                                                    |

### `queryMode`

- **`LOCAL`** — one `GET` to `value`, then filter / sort / page in memory. Suited to endpoints without query capabilities.
- **`REMOTE`** — appends `pageIndex`, `pageSize`, `sort`, and equality conditions as query parameters. The endpoint must return `{ "total": n, "list": [...] }` (a plain array is also accepted, with `total` = array size).

## Example

```java
@Getter
@Setter
@Erupt(name = "GitHub User", primaryKeyCol = "id")
@EruptHttp(
    value = "https://api.github.com/users",
    headers = { "Accept: application/vnd.github+json", "Authorization: Bearer ghp_xxx" },
    queryMode = EruptHttp.QueryMode.LOCAL,
    timeout = 15
)
@EruptDataProcessor(EruptHttpDataService.DATA_PROCESSOR)
public class GhUser {

    @EruptField(views = @View(title = "ID"))
    private Long id;

    @EruptField(
        views = @View(title = "Login"),
        edit = @Edit(title = "Login", search = @Search)
    )
    private String login;

    @EruptField(views = @View(title = "Type"))
    private String type;

    @EruptField(views = @View(title = "Avatar"))
    private String avatar_url;
}
```

## Operations

Full CRUD assuming the endpoint implements the shape above. If your service is read-only, override `addData` / `editData` / `deleteData` in a subclass to throw an i18n-friendly error, or restrict permissions in the `@Erupt(power = ...)` layer.
