package xyz.erupt.revision.model;

/**
 * What the revision records: the whole record appearing, some of its fields changing, or the
 * whole record going away.
 */
public enum RevisionOperation {
    ADD, UPDATE, DELETE
}
