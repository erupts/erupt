package xyz.erupt.memory.service;

import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.view.EruptModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Writable in-memory data source: full CRUD backed by a ConcurrentHashMap keyed by the
 * model's primary key. Data lives for the process lifetime only — suited for prototypes,
 * runtime-registered models and tests rather than durable storage.
 * <p>
 * Missing primary keys are generated on add: numeric fields get an incrementing sequence,
 * everything else gets a UUID string.
 *
 * @author YuePeng
 */
public abstract class EruptMemoryRepository<T> extends EruptMemoryDataService<T> {

    // Keys are normalized to strings so lookups stay insensitive to Long/String id types
    private final Map<String, T> store = new ConcurrentHashMap<>();

    private final AtomicLong sequence = new AtomicLong();

    @Override
    protected List<T> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        return new ArrayList<>(store.values());
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        T bean = store.get(id.toString());
        return null != bean ? bean : super.findDataById(eruptModel, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addData(EruptModel eruptModel, Object object) {
        Object id = this.readValue(eruptModel, (T) object, eruptModel.getErupt().primaryKeyCol());
        if (null == id) id = this.generatePrimaryKey(eruptModel, object);
        store.put(id.toString(), (T) object);
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        store.put(this.requirePrimaryKey(eruptModel, object).toString(), this.cast(object));
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        store.remove(this.requirePrimaryKey(eruptModel, object).toString());
    }

    @SuppressWarnings("unchecked")
    private T cast(Object object) {
        return (T) object;
    }

    private Object requirePrimaryKey(EruptModel eruptModel, Object object) {
        Object id = this.readValue(eruptModel, this.cast(object), eruptModel.getErupt().primaryKeyCol());
        if (null == id) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("memory.primary_key_missing"));
        return id;
    }

    @SuppressWarnings("unchecked")
    private Object generatePrimaryKey(EruptModel eruptModel, Object object) {
        String primaryKey = eruptModel.getErupt().primaryKeyCol();
        if (object instanceof Map) {
            Object id = sequence.incrementAndGet();
            ((Map<String, Object>) object).put(primaryKey, id);
            return id;
        }
        try {
            Field field = null;
            for (Class<?> c = object.getClass(); null != c && c != Object.class; c = c.getSuperclass()) {
                try {
                    field = c.getDeclaredField(primaryKey);
                    break;
                } catch (NoSuchFieldException ignore) {
                }
            }
            if (null == field) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("memory.primary_key_missing"));
            field.setAccessible(true);
            Object id;
            Class<?> type = field.getType();
            if (type == Long.class || type == long.class) {
                id = sequence.incrementAndGet();
            } else if (type == Integer.class || type == int.class) {
                id = (int) sequence.incrementAndGet();
            } else {
                id = UUID.randomUUID().toString();
            }
            field.set(object, id);
            return id;
        } catch (IllegalAccessException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

}
