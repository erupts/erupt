# erupt-generator

Code generator for Erupt entities.

Reads the schema of any datasource the application has registered and turns a table into an
annotated java entity (`@Erupt` + `@EruptField`) ready to drop into the project — the fast path
when an existing database has to be brought under Erupt.

## Import from database

The *Import from Database* button on the generator list picks a datasource, a database and the
tables to read — the three are chained, each one reloads the next.

`Package` is written as the package statement of every generated class. `Ignore Columns` drops
columns the entity should not carry; opening the list reads the columns of the tables picked so
far, the ones several tables share leading the way, and a pattern such as `tenant_*` can still be
typed by hand. `Parent Class` decides which columns are inherited instead of declared — the
`MetaModel` / `HyperModel` families keep the audit trail, their `Vo` variants also show it.

What JDBC metadata becomes:

| Source | Becomes |
|---|---|
| table name | entity class name, the erupt `e_` prefix dropped (`e_order` → `Order`) |
| table / column comment | `@Erupt(name)`, `@View(title)`, `@Edit(title)` |
| jdbc type + size | `EditType` and java type (`bigint` → `Long`, `decimal(12,2)` → `BigDecimal`) |
| column name | `PASSWORD`, `IMAGE`, `ATTACHMENT`, `ICON`, `COLOR` when the name says so |
| comment of a code column | `EditType.CHOICE` with the `@VL` pairs the comment documents (`0-disabled 1-enabled`) |
| `not null` | `@Edit(notNull = true)` |
| single column unique index | `@Column(unique = true)` |
| foreign key | `@ManyToOne` + `REFERENCE_TABLE`, labelled by a column the referenced table really has |
| primary key | inherited from the parent model when it is named `id`, otherwise `@Id` + `primaryKeyCol` |

Comments are read from `REMARKS`, except on MySQL / MariaDB where they only live in
`information_schema` unless the connection was opened with `useInformationSchema`.

Everything imported lands in an ordinary erupt table, so the guesses can be corrected by hand
before the code leaves: *Preview* reads one class in the admin code editor with copy, download and
fullscreen, *Download* saves the selected rows as a java file or, for several of them, as a zip
whose entries carry their package path so the archive unzips straight over a source tree. `Component Config` on a field overrides the component configuration
the edit type would produce.

## Limitations

- A table needs exactly one primary key column, a composite or missing key is rejected.
- Enum values guessed from a comment follow the `0-disabled 1-enabled` shape; anything more
  exotic stays a plain number.
- A class name already taken by a registered erupt model is only flagged as a `//FIXME` comment
  in the generated code, nothing stops the import.
