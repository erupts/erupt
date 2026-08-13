# erupt-data-redis

Redis-backed data source for Erupt. Bind a `@Erupt` model to a set of Redis hashes — each row is one hash stored at `<prefix><primary-key>`, with model fields mapped to hash fields.

Suited to configuration, feature flags, session-like entities, and small runtime dictionaries — Redis storage semantics are unbounded and the module reads all keys under the prefix on each list, so avoid large data sets.

## Configuration

Standard Spring Boot properties:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: ""
      database: 0
```

## Annotation

`@EruptRedis`

| Attribute | Default | Description                                              |
|-----------|---------|----------------------------------------------------------|
| `value`   | —       | Key prefix; each row stored at `<value><primary-key>`    |

## Example

```java
@Getter
@Setter
@Erupt(name = "Feature Flag", primaryKeyCol = "key")
@EruptRedis("feature:")
@EruptDataProcessor(EruptRedisDataService.DATA_PROCESSOR)
public class FeatureFlag {

    @EruptField(views = @View(title = "Key"))
    private String key;

    @EruptField(
        views = @View(title = "Enabled"),
        edit = @Edit(title = "Enabled", type = EditType.BOOLEAN)
    )
    private Boolean enabled;

    @EruptField(edit = @Edit(title = "Description"))
    private String description;

    @EruptField(edit = @Edit(title = "Updated By"))
    private String updatedBy;
}
```

The `enabled = true` flag `home_banner` would live at Redis key `feature:home_banner`, hash-encoded.

## Operations

Full CRUD:

- **List**: `SCAN` under the prefix, `HGETALL` per key.
- **Find by id**: `HGETALL` on `<prefix><id>`.
- **Add / edit**: `HSET` all field values; primary key value doubles as the key suffix and must be supplied.
- **Delete**: `DEL` on `<prefix><id>`.

## Gotchas

- **Flat models only.** Nested objects and collections are stored as JSON strings; deserialising them back to typed collections is not automatic — declare them as `String` if you need round-trip fidelity.
- **Primary key required on add.** No auto-generation — supply the key value yourself.
- List operations `SCAN` all matching keys and issue a `HGETALL` per row; do not point this at prefixes with thousands of entries.
