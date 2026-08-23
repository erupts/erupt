# erupt-data-memory

Writable in-memory data source for Erupt. Rows live for the process lifetime in a `ConcurrentHashMap` keyed by the model's primary key. Suited to prototypes, tests, runtime-registered dynamic models, and demos — not to durable production storage.

Provides full CRUD out of the box, so it doubles as the "quickest possible" Erupt example when you want a working admin page without setting up a database.

## Usage

Extend `EruptMemoryRepository<T>` and register it as a Spring bean. The class name becomes the `DATA_PROCESSOR` id — reference it from `@EruptDataProcessor`.

```java
@Service
public class TaskRepository extends EruptMemoryRepository<Task> {
    public static final String DATA_PROCESSOR = "MEMORY_TASK";
    static { DataProcessorManager.register(DATA_PROCESSOR, TaskRepository.class); }
}

@Getter
@Setter
@Erupt(name = "Task", primaryKeyCol = "id")
@EruptDataProcessor(TaskRepository.DATA_PROCESSOR)
public class Task {

    @EruptField(views = @View(title = "ID"))
    private Long id;

    @EruptField(
        views = @View(title = "Title"),
        edit = @Edit(title = "Title", notNull = true, search = @Search)
    )
    private String title;

    @EruptField(
        views = @View(title = "Done"),
        edit = @Edit(title = "Done", type = EditType.BOOLEAN)
    )
    private Boolean done;
}
```

## Primary key generation

If the primary key value is null on add:

- `Long` / `long` / `Integer` / `int` fields get an incrementing sequence
- Everything else gets a `UUID.randomUUID().toString()`

Supply a non-null primary key on add if you need stable, meaningful ids.

## Operations

Full CRUD: list / find / add / edit / delete. Filtering, sorting, and paging are evaluated in memory by the base class — same semantics as the persistent implementations, so you can prototype against memory and switch to JPA / MongoDB without changing your annotations.
