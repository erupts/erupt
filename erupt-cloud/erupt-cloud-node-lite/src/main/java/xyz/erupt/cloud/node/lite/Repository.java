package xyz.erupt.cloud.node.lite;

import java.util.List;

/**
 * Storage of one model: implement these methods to put an erupt model on any storage. Records are instances of
 * the model class; {@link Model#get} / {@link Model#set} read and write their columns.
 */
public interface Repository<T> {

    /** Called once when the model is registered. */
    default void bind(Model<T> model) {
    }

    /** The total matching {@code query} and one page of records. */
    Page<T> page(Query query);

    /** The record, or null when there is none. */
    T get(Object pk);

    /** Stores a new record and sets its generated primary key. */
    void add(T record);

    /** Stores every column of an existing record. */
    void update(T record);

    void delete(List<Object> pks);

    record Page<T>(long total, List<T> records) {
    }
}
