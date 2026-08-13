# erupt-data-es

Elasticsearch data source for Erupt, built on Spring Data Elasticsearch. Bind a `@Erupt` model to an ES index — filtering, sorting, and paging are pushed down to `_search`, and the base engine handles drill / condition-string parsing on top.

`spring-boot-starter-data-elasticsearch` is pulled in transitively.

## Configuration

Standard Spring Boot properties:

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
    username: elastic
    password: changeme
```

## Example

```java
@Getter
@Setter
@Document(indexName = "product")
@Erupt(name = "Product", primaryKeyCol = "id")
@EruptDataProcessor(EruptEsDataService.DATA_PROCESSOR)
public class Product {

    @Id
    @EruptField(views = @View(title = "ID"))
    private String id;

    @Field(type = FieldType.Keyword)
    @EruptField(
        views = @View(title = "SKU"),
        edit = @Edit(title = "SKU", notNull = true, search = @Search)
    )
    private String sku;

    @Field(type = FieldType.Text, fielddata = true)
    @EruptField(
        views = @View(title = "Name"),
        edit = @Edit(title = "Name", search = @Search(vague = true))
    )
    private String name;

    @Field(type = FieldType.Double)
    @EruptField(views = @View(title = "Price"), edit = @Edit(title = "Price"))
    private Double price;

    @Field(type = FieldType.Date)
    @EruptField(views = @View(title = "Created"))
    private Date createdAt;
}
```

## Operations

Full CRUD: list / find / add / edit / delete. Add and edit both go through Spring Data's `save`, which is an upsert on the document `_id`.

## Gotchas

- **Use `FieldType.Keyword` (or a multi-field) for string columns you want to filter or sort on exactly.** `FieldType.Text` values are analysed and won't match equality queries.
- The document `_id` maps to the model's `@Id` (or the field named `id`) — declare it explicitly.
- After a write the service issues an index `refresh` so the admin table re-query sees the change immediately; for hot indices in production consider disabling this by extending the service.
