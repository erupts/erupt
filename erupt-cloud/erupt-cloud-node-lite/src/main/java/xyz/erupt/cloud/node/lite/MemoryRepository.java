package xyz.erupt.cloud.node.lite;

import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Direction;
import xyz.erupt.annotation.query.Sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/** Records kept in process memory, copied in and out; the primary key is a sequence starting at 1. */
public final class MemoryRepository<T> implements Repository<T> {

    private final Map<Object, T> rows = new LinkedHashMap<>();
    private final List<T> seed;
    private Model<T> model;
    private long seq;

    /** Seed records, added (and numbered) when the model is registered. */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public MemoryRepository(T... seed) {
        this.seed = List.of(seed);
    }

    @Override
    public void bind(Model<T> model) {
        this.model = model;
        seed.forEach(this::add);
    }

    @Override
    public synchronized Page<T> page(Query query) {
        if (!query.filters().isEmpty()) throw new IllegalStateException("MemoryRepository cannot apply the SQL of beforeFetch");
        List<T> match = new ArrayList<>();
        for (T r : rows.values()) {
            if (query.conditions().stream().allMatch(c -> matches(model.get(r, c.getKey()), c))) match.add(copy(r));
        }
        Comparator<T> order = null;
        for (Sort s : query.sort()) {
            // ascending puts nulls last; descending reverses the whole order, nulls first
            Comparator<T> by = Comparator.comparing((Function<T, Object>) r -> model.get(r, s.getField()), Comparator.nullsLast(Values::compare));
            if (s.getDirection() == Direction.DESC) by = by.reversed();
            order = order == null ? by : order.thenComparing(by);
        }
        if (order != null) match.sort(order);
        int from = Math.min(query.offset(), match.size());
        return new Page<>(match.size(), match.subList(from, Math.min(from + query.limit(), match.size())));
    }

    @Override
    public synchronized T get(Object pk) {
        T row = rows.get(key(pk));
        return row == null ? null : copy(row);
    }

    @Override
    public synchronized void add(T record) {
        model.set(record, model.pk(), ++seq);
        rows.put(key(model.get(record, model.pk())), copy(record));
    }

    @Override
    public synchronized void update(T record) {
        Object key = key(model.get(record, model.pk()));
        if (rows.containsKey(key)) rows.put(key, copy(record));
    }

    @Override
    public synchronized void delete(List<Object> pks) {
        pks.forEach(pk -> rows.remove(key(pk)));
    }

    private T copy(T record) {
        T out = model.newInstance();
        for (String c : model.columns()) model.set(out, c, model.get(record, c));
        return out;
    }

    private static Object key(Object pk) {
        return Values.normalize(pk);
    }

    private static boolean matches(Object value, Condition c) {
        switch (c.getExpression()) {
            case NULL:
                return value == null;
            case NOT_NULL:
                return value != null;
            default:
                if (value == null) return false;
        }
        Object expect = c.getValue();
        List<?> list = expect instanceof List<?> l ? l : List.of();
        return switch (c.getExpression()) {
            case EQ -> Values.same(value, expect);
            case NEQ -> !Values.same(value, expect);
            case LIKE -> contains(value, expect);
            case NOT_LIKE -> !contains(value, expect);
            case IN -> list.stream().anyMatch(v -> Values.same(value, v));
            case NOT_IN -> list.stream().noneMatch(v -> Values.same(value, v));
            case RANGE -> Values.compare(list.get(0), value) <= 0 && Values.compare(value, list.get(1)) <= 0;
            case GT -> Values.compare(value, expect) > 0;
            case GTE -> Values.compare(value, expect) >= 0;
            case LT -> Values.compare(value, expect) < 0;
            case LTE -> Values.compare(value, expect) <= 0;
            default -> throw new IllegalArgumentException("illegal operator " + c.getExpression());
        };
    }

    private static boolean contains(Object value, Object text) {
        return Values.text(value).toLowerCase(Locale.ROOT).contains(Values.text(text).toLowerCase(Locale.ROOT));
    }
}
