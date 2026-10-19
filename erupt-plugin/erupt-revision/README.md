# erupt-revision

Field-level change history for every erupt record, with rollback and a recycle bin. Add the module
and every add, update and delete that goes through the erupt data pipeline leaves a row in
`e_record_revision`; a model opts out with `@Power(revision = false)`.

```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-revision</artifactId>
    <version>${erupt.version}</version>
</dependency>
```

## What is recorded

One row per change: model, record id, a version counter per record, the operation (ADD, UPDATE,
DELETE), who did it, and the field-level diff as JSON `{field, title, before, after}`. Values are
the record's masked JSON, so a password never lands here; a picked reference is kept as its key
plus display label, owned sub-rows in full.

The record panel shows the history and lets an update be rolled back: the `before` values of that
revision go back onto the current record through the regular update pipeline (validation, DataProxy,
a new revision), fields the revision did not touch keep their current value.

## Recycle bin

*System Management → Record Revision* is the recycle bin too: filter on operation DELETE and every
record deleted through erupt is there, with a short summary of what the row held (a delete that was
restored since is marked). The Restore row button is enabled on delete revisions only.

- **Restore** rebuilds the row from the delete snapshot through the add pipeline, so validation,
  DataProxy hooks and permissions apply, and the restore is itself an ADD revision. A generated key
  cannot be reused: the record may come back under a new id, and its history moves with it.
- Deleting a revision row is the table's ordinary delete: on a delete revision it drops the only copy
  of the deleted row, so the record leaves the bin for good; on any other it prunes history.

What cannot come back: password fields (never stored), and a reference to a row that was deleted
too (restore that one first). Restore needs add power on the model.

API: `GET /erupt-api/revision/{erupt}/{id}` lists the history, `POST .../{revisionId}/rollback` and
`POST /erupt-api/revision/{erupt}/{id}/restore` (answers the primary key, possibly new).
