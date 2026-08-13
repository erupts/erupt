# erupt-data-mongodb

MongoDB data source for Erupt, built on Spring Data MongoDB. Use it as a drop-in replacement for `erupt-data-jpa` when your data lives in MongoDB — the core engine's CRUD, pagination, search, and drill features all work identically on document collections.

## Configuration

Standard Spring Boot properties:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/erupt
```

## Example

```java
@Getter
@Setter
@Document("customer")
@Erupt(name = "Customer", primaryKeyCol = "id")
@EruptDataProcessor(EruptMongodbImpl.DATA_PROCESSOR)
public class Customer {

    @Id
    @EruptField(views = @View(title = "ID"))
    private String id;

    @EruptField(
        views = @View(title = "Name"),
        edit = @Edit(title = "Name", notNull = true, search = @Search)
    )
    private String name;

    @EruptField(
        views = @View(title = "Tags"),
        edit = @Edit(title = "Tags", type = EditType.TAG)
    )
    private List<String> tags;

    @EruptField(
        views = @View(title = "Joined"),
        edit = @Edit(title = "Joined", type = EditType.DATE)
    )
    private Date joinedAt;
}
```

## Operations

Full CRUD: list / find / add / edit / delete / drill / column-query. Search conditions (equality, LIKE, range, IN, sort) are pushed down to MongoDB via `Criteria`. Paging uses skip / limit.

## Gotchas

- `@Id`-annotated field maps to MongoDB `_id`; declare `@Erupt(primaryKeyCol = "id")` and add a `String id` field so drill and edit-form addressing work.
- Nested documents and arrays are supported; expose them with matching field types on the model.
