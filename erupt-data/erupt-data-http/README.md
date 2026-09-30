# erupt-data-http

REST-backed data source for Erupt. Bind a `@Erupt` model to a JSON HTTP endpoint — Erupt drives list / add / edit / delete against your service without any DAO or Spring proxy on the client side.

Uses the JDK's built-in `HttpClient` — no extra runtime dependency beyond `erupt-core`.

## Expected endpoint shape

```
GET    {value}         → [ {...}, ... ]   or   { "total": n, "list": [ ... ] }
GET    {value}/{id}    → { ... }
POST   {value}         add
PUT    {value}/{id}    edit   (PATCH with editMethod = PATCH)
DELETE {value}/{id}    delete
```

Anything else — a different envelope, other paging parameter names, per-user auth — is a matter of configuration or one adapter class, see below.

## Annotation

`@EruptHttp`

| Attribute    | Default                 | Description                                                                                 |
|--------------|-------------------------|---------------------------------------------------------------------------------------------|
| `value`      | —                       | Resource base URL. Template, see *Dynamic values*                                           |
| `headers`    | `{}`                    | Extra request headers as `"Name: Value"` strings. Templates                                 |
| `queryMode`  | `LOCAL`                 | `LOCAL` fetches the full list and filters in memory; `REMOTE` delegates paging              |
| `paging`     | `PAGE_SIZE`             | REMOTE parameter names: `PAGE_SIZE` (`pageIndex`/`pageSize`), `PAGE_PER_PAGE` (`page`/`per_page`), `OFFSET_LIMIT` (`offset`/`limit`) |
| `listPath`   | `""`                    | Dotted path to the record array, e.g. `data.items`. Empty accepts a plain array or `list`   |
| `totalPath`  | `""`                    | Dotted path to the total count, e.g. `meta.count`. Empty reads `total`, else the array size |
| `itemPath`   | `""`                    | Dotted path to the record in a single-object response, e.g. `data`                          |
| `editMethod` | `PUT`                   | `PUT` or `PATCH` for edit                                                                   |
| `adapter`    | `EruptHttpAdapter.class` | Hooks for everything the attributes cannot express                                         |
| `timeout`    | `10`                    | Request timeout in seconds                                                                  |

### `queryMode`

- **`LOCAL`** — one `GET` to `value`, then filter / sort / page in memory. Suited to endpoints without query capabilities.
- **`REMOTE`** — appends the `paging` pair, `sort` (`field asc, field2 desc`) and equality conditions as `key=value` query parameters; the response is read through `listPath` / `totalPath`.

## Dynamic values

`value` and every header value are templates resolved on **each request**, so nothing secret or session-bound has to be a compile-time constant:

| Syntax                     | Resolves to                                                        |
|----------------------------|--------------------------------------------------------------------|
| `${erupt.http.gh.token}`   | a Spring property — keep credentials in configuration / env vars   |
| `#{user.tenantId}`         | SpEL on the current session: `user` is the `MetaUser` (`uid`, `account`, `name`, `tenantId`), `null` outside a request |
| `#{@tokenService.token()}` | SpEL calling any Spring bean — e.g. a cached, self-refreshing OAuth token |

```java
@EruptHttp(
    value = "https://api.example.com/tenants/#{user.tenantId}/orders",
    headers = { "Authorization: Bearer #{@oauth.accessToken()}", "X-Api-Key: ${example.api-key}" }
)
```

## Adapter

For anything beyond templates, implement `EruptHttpAdapter` and reference it with `adapter = MyAdapter.class`. Every method has a default, override only what your API does differently. A class annotated `@Component` is taken from the Spring context, so it can hold injected services and token caches; otherwise it is instantiated once per model.

| Hook                                  | Default                                             | Override for                                      |
|---------------------------------------|-----------------------------------------------------|---------------------------------------------------|
| `url(url, model)`                     | unchanged                                           | path rewriting, host switching                    |
| `request(builder, model)`             | nothing                                             | HMAC signatures, per-user tokens from `MetaContext` |
| `query(params, page, conditions, model)` | equality conditions as `key=value`                | `name_like`, `filter=` expressions, cursor tokens |
| `body(object, method, model)`         | `gson.toJson(object)`                               | `{ "data": {...} }` wrapping, dropping the id on POST |
| `list(response, model)`               | `listPath`                                          | non-JSON or irregular list shapes                 |
| `total(response, model)`              | `totalPath`, `null` if absent                       | counts in headers such as `X-Total-Count`         |
| `item(response, model)`               | `itemPath` → model class                            | irregular single-object shapes                    |
| `error(response, method, url)`        | status + `message` / `msg` / `error` from the body  | your API's error envelope                         |

```java
@Component
public class GithubAdapter implements EruptHttpAdapter {

    @Override
    public Long total(HttpResponse<String> response, EruptModel model) {
        // GitHub paginates through the Link header; report "one more page" while it has next
        return response.headers().firstValue("link").filter(l -> l.contains("rel=\"next\"")).isPresent()
                ? (long) Page.PAGE_MAX_DATA : null;
    }

    @Override
    public void query(Map<String, String> params, Page page, List<Condition> conditions, EruptModel model) {
        conditions.stream().filter(c -> QueryExpression.LIKE == c.getExpression())
                .forEach(c -> params.put("q", c.getValue() + " in:" + c.getKey()));
    }
}
```

## Example

```java
@Getter
@Setter
@Erupt(name = "GitHub User", primaryKeyCol = "id")
@EruptHttp(
    value = "https://api.github.com/users",
    headers = { "Accept: application/vnd.github+json", "Authorization: Bearer ${github.token}" },
    queryMode = EruptHttp.QueryMode.REMOTE,
    paging = EruptHttp.Paging.PAGE_PER_PAGE,
    adapter = GithubAdapter.class
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
