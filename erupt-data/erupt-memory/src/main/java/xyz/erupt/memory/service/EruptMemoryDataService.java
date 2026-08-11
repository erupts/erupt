package xyz.erupt.memory.service;

import lombok.SneakyThrows;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Direction;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.util.TypeUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Read-only in-memory data source: subclasses only supply beans (or maps) via
 * {@link #data(EruptQuery)}; searching, drill condition strings, sorting and paging
 * are all evaluated here with the same semantics as the persistent implementations.
 * <p>
 * Concrete services must self-register, e.g.
 * <pre>
 *   static { DataProcessorManager.register(DATA_PROCESSOR, MyDataService.class); }
 * </pre>
 * and be referenced from the model with {@code @EruptDataProcessor(DATA_PROCESSOR)}.
 *
 * @author YuePeng
 */
public abstract class EruptMemoryDataService<T> implements IEruptDataService {

    protected abstract List<T> data(EruptModel eruptModel, EruptQuery eruptQuery);

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        String primaryKey = eruptModel.getErupt().primaryKeyCol();
        for (T bean : this.data(eruptModel, EruptQuery.builder().build())) {
            if (this.eq(this.readValue(eruptModel, bean, primaryKey), id)) return bean;
        }
        return null;
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        List<Map<String, Object>> rows = this.rows(eruptModel, eruptQuery);
        List<Sort> sorts = null == page.getSort() || page.getSort().isEmpty()
                ? Sort.toSortList(eruptModel.getErupt().orderBy()) : page.getSort();
        if (!sorts.isEmpty()) {
            rows.sort((a, b) -> {
                for (Sort sort : sorts) {
                    int c = this.compareValue(a.get(sort.getField()), b.get(sort.getField()));
                    if (0 != c) return sort.getDirection() == Direction.DESC ? -c : c;
                }
                return 0;
            });
        }
        page.setTotal((long) rows.size());
        int from = Math.min((page.getPageIndex() - 1) * page.getPageSize(), rows.size());
        page.setList(rows.subList(from, Math.min(from + page.getPageSize(), rows.size())));
        return page;
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        return this.rows(eruptModel, eruptQuery).stream().map(row -> {
            Map<String, Object> map = new LinkedHashMap<>();
            for (Column column : columns) map.put(column.getAlias(), row.get(column.getName()));
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        throw new EruptWebApiRuntimeException(I18nTranslate.$translate("memory.read_only"));
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        throw new EruptWebApiRuntimeException(I18nTranslate.$translate("memory.read_only"));
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        throw new EruptWebApiRuntimeException(I18nTranslate.$translate("memory.read_only"));
    }

    private List<Map<String, Object>> rows(EruptModel eruptModel, EruptQuery eruptQuery) {
        List<Condition> conditions = this.mergeConditions(eruptQuery);
        return this.data(eruptModel, eruptQuery).stream()
                .map(bean -> this.toRow(eruptModel, bean))
                .filter(row -> this.match(eruptModel, row, conditions))
                .collect(Collectors.toList());
    }

    protected Map<String, Object> toRow(EruptModel eruptModel, T bean) {
        Map<String, Object> row = new LinkedHashMap<>();
        if (bean instanceof Map) {
            ((Map<?, ?>) bean).forEach((k, v) -> row.put(String.valueOf(k), v));
            return row;
        }
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            row.put(fieldModel.getFieldName(), this.readField(bean, fieldModel.getField()));
        }
        String primaryKey = eruptModel.getErupt().primaryKeyCol();
        if (!row.containsKey(primaryKey)) row.put(primaryKey, this.readValue(eruptModel, bean, primaryKey));
        return row;
    }

    protected Object readValue(EruptModel eruptModel, Object bean, String fieldName) {
        if (bean instanceof Map) return ((Map<?, ?>) bean).get(fieldName);
        Field field = this.fieldOf(eruptModel, bean.getClass(), fieldName);
        return null == field ? null : this.readField(bean, field);
    }

    private Field fieldOf(EruptModel eruptModel, Class<?> clazz, String fieldName) {
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        if (null != fieldModel && null != fieldModel.getField()) return fieldModel.getField();
        for (Class<?> c = clazz; null != c && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignore) {
            }
        }
        return null;
    }

    @SneakyThrows
    private Object readField(Object bean, Field field) {
        field.setAccessible(true);
        return field.get(bean);
    }

    protected List<Condition> mergeConditions(EruptQuery eruptQuery) {
        List<Condition> conditions = new ArrayList<>();
        Optional.ofNullable(eruptQuery.getConditions()).ifPresent(conditions::addAll);
        Optional.ofNullable(eruptQuery.getConditionStrings()).ifPresent(strings ->
                strings.stream().map(this::parseConditionString).filter(Objects::nonNull).forEach(conditions::add));
        return conditions;
    }

    // Parses the "Entity.field = value" / "Entity.field = 'value'" equality fragments produced by
    // drillProcess and static filters; anything more complex is ignored (same behavior as mongodb impl)
    protected Condition parseConditionString(String conditionString) {
        if (null == conditionString || conditionString.trim().isEmpty()) return null;
        String trimmed = conditionString.trim();
        int eqIdx = trimmed.indexOf('=');
        if (eqIdx < 0) return null;
        String lhs = trimmed.substring(0, eqIdx).trim();
        String rhs = trimmed.substring(eqIdx + 1).trim();
        int dotIdx = lhs.lastIndexOf('.');
        String fieldName = dotIdx >= 0 ? lhs.substring(dotIdx + 1) : lhs;
        if (fieldName.isEmpty() || rhs.isEmpty()) return null;
        Object value;
        if (rhs.startsWith("'") && rhs.endsWith("'") && rhs.length() >= 2) {
            value = rhs.substring(1, rhs.length() - 1);
        } else {
            value = rhs;
        }
        return new Condition(fieldName, value, QueryExpression.EQ);
    }

    protected boolean match(EruptModel eruptModel, Map<String, Object> row, List<Condition> conditions) {
        for (Condition condition : conditions) {
            Object value = row.get(condition.getKey());
            Object target = this.convertTarget(eruptModel, condition.getKey(), condition.getValue());
            boolean pass = switch (condition.getExpression()) {
                case EQ -> this.eq(value, target);
                case NEQ -> !this.eq(value, target);
                case GT -> null != value && this.compareValue(value, target) > 0;
                case GTE -> null != value && this.compareValue(value, target) >= 0;
                case LT -> null != value && this.compareValue(value, target) < 0;
                case LTE -> null != value && this.compareValue(value, target) <= 0;
                case LIKE -> null != value && value.toString().toLowerCase().contains(String.valueOf(target).toLowerCase());
                case NOT_LIKE -> null == value || !value.toString().toLowerCase().contains(String.valueOf(target).toLowerCase());
                case RANGE -> {
                    List<?> range = (List<?>) target;
                    yield null != value && this.compareValue(value, range.get(0)) >= 0 && this.compareValue(value, range.get(1)) <= 0;
                }
                case IN -> target instanceof Collection
                        && ((Collection<?>) target).stream().anyMatch(it -> this.eq(value, it));
                case NOT_IN -> !(target instanceof Collection)
                        || ((Collection<?>) target).stream().noneMatch(it -> this.eq(value, it));
                case NULL -> null == value;
                case NOT_NULL -> null != value;
                default -> true;
            };
            if (!pass) return false;
        }
        return true;
    }

    // Frontend condition values arrive as strings; align them with the field type so
    // numeric / date comparisons behave like the persistent implementations
    protected Object convertTarget(EruptModel eruptModel, String key, Object value) {
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(key);
        if (null == fieldModel || null == fieldModel.getField() || null == value) return value;
        Class<?> type = fieldModel.getField().getType();
        if (value instanceof Collection) {
            return ((Collection<?>) value).stream()
                    .map(it -> TypeUtil.typeStrConvertObject(it, type)).collect(Collectors.toList());
        }
        return TypeUtil.typeStrConvertObject(value, type);
    }

    protected boolean eq(Object value, Object target) {
        if (Objects.equals(value, target)) return true;
        if (null == value || null == target) return false;
        if (value instanceof Number && target instanceof Number) {
            return ((Number) value).doubleValue() == ((Number) target).doubleValue();
        }
        return value.toString().equals(target.toString());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected int compareValue(Object a, Object b) {
        if (Objects.equals(a, b)) return 0;
        if (null == a) return -1;
        if (null == b) return 1;
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        }
        if (a instanceof Comparable && a.getClass().isInstance(b)) return ((Comparable) a).compareTo(b);
        return a.toString().compareTo(b.toString());
    }

}
