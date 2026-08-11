package xyz.erupt.monitor.service;

import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Direction;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Base class for read-only erupt data sources backed by in-process memory:
 * subclasses supply rows, conditions / sorting / paging are evaluated here.
 *
 * @author YuePeng
 */
public abstract class MemoryDataService implements IEruptDataService {

    protected abstract List<Map<String, Object>> rows(EruptQuery eruptQuery);

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        List<Map<String, Object>> rows = this.rows(eruptQuery).stream()
                .filter(it -> this.match(it, eruptQuery.getConditions())).collect(Collectors.toList());
        if (null != page.getSort() && !page.getSort().isEmpty()) {
            Comparator<Map<String, Object>> comparator = null;
            for (Sort sort : page.getSort()) {
                @SuppressWarnings("unchecked")
                Comparator<Map<String, Object>> c = Comparator.comparing(it ->
                        (Comparable<Object>) it.get(sort.getField()), Comparator.nullsFirst(Comparator.naturalOrder()));
                if (sort.getDirection() == Direction.DESC) c = c.reversed();
                comparator = null == comparator ? c : comparator.thenComparing(c);
            }
            rows.sort(comparator);
        }
        page.setTotal((long) rows.size());
        int from = Math.min((page.getPageIndex() - 1) * page.getPageSize(), rows.size());
        page.setList(rows.subList(from, Math.min(from + page.getPageSize(), rows.size())));
        return page;
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        return this.rows(eruptQuery).stream()
                .filter(it -> this.match(it, eruptQuery.getConditions()))
                .map(it -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    for (Column column : columns) map.put(column.getAlias(), it.get(column.getName()));
                    return map;
                }).collect(Collectors.toList());
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        throw new UnsupportedOperationException();
    }

    private boolean match(Map<String, Object> row, List<Condition> conditions) {
        if (null == conditions) return true;
        for (Condition condition : conditions) {
            Object value = row.get(condition.getKey());
            Object target = condition.getValue();
            boolean pass = switch (condition.getExpression()) {
                case LIKE -> null != value && value.toString().toLowerCase().contains(target.toString().toLowerCase());
                case EQ -> this.eq(value, target);
                case NEQ -> !this.eq(value, target);
                case IN -> target instanceof Collection
                        && ((Collection<?>) target).stream().anyMatch(it -> this.eq(value, it));
                case NULL -> null == value;
                case NOT_NULL -> null != value;
                default -> true;
            };
            if (!pass) return false;
        }
        return true;
    }

    private boolean eq(Object value, Object target) {
        if (Objects.equals(value, target)) return true;
        if (null == value || null == target) return false;
        if (value instanceof Number && target instanceof Number) {
            return ((Number) value).doubleValue() == ((Number) target).doubleValue();
        }
        return value.toString().equals(target.toString());
    }

}
