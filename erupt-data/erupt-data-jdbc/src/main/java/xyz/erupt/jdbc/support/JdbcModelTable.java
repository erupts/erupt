package xyz.erupt.jdbc.support;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Direction;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.TypeUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generic "one erupt model ↔ one table" SQL access on top of {@link NamedParameterJdbcTemplate}.
 * <p>
 * Filtering, sorting and paging are pushed down as SQL; condition values are always bound as
 * named parameters and condition keys / sort fields must be declared model fields, both guard
 * against SQL injection. Non-scalar fields (Map / Collection / nested beans) round-trip as JSON
 * text, so a plain table can carry reference and multi-value columns without a join model.
 * Map-valued fields are additionally flattened into {@code field_key} entries on list rows,
 * matching the JPA implementation's reference column convention.
 * <p>
 * Subclasses customise SQL generation through the protected hooks; the class is intentionally
 * not a Spring bean so several data services can each own a differently tuned instance without
 * bean-type ambiguity.
 *
 * @author YuePeng
 * date 2026-09-09
 */
public class JdbcModelTable {

    protected final Gson gson = GsonFactory.getGson();

    private final Function<EruptModel, NamedParameterJdbcTemplate> templates;

    private final Function<EruptModel, String> tables;

    public JdbcModelTable(Function<EruptModel, NamedParameterJdbcTemplate> templates, Function<EruptModel, String> tables) {
        this.templates = templates;
        this.tables = tables;
    }

    public Object findById(EruptModel model, Object id) {
        String pk = model.getErupt().primaryKeyCol();
        List<Map<String, Object>> rows = this.template(model).queryForList(
                "select * from " + this.from(model) + " where " + pk + " = :id",
                Collections.singletonMap("id", this.convertTarget(model.getEruptFieldMap().get(pk), id)));
        return rows.isEmpty() ? null : this.toBean(model, rows.get(0));
    }

    public Page query(EruptModel model, Page page, EruptQuery eruptQuery) {
        Map<String, Object> params = new HashMap<>();
        String where = this.where(model, eruptQuery, params);
        NamedParameterJdbcTemplate template = this.template(model);
        page.setTotal(template.queryForObject("select count(*) from " + this.from(model) + where, params, Long.class));
        String sql = "select * from " + this.from(model) + where + this.orderBy(model, page)
                + " limit " + page.getPageSize() + " offset " + (page.getPageIndex() - 1) * page.getPageSize();
        page.setList(template.queryForList(sql, params).stream()
                .map(row -> this.normalizeRow(model, row)).collect(Collectors.toList()));
        return page;
    }

    public Collection<Map<String, Object>> queryColumn(EruptModel model, List<Column> columns, EruptQuery eruptQuery) {
        Map<String, Object> params = new HashMap<>();
        String where = this.where(model, eruptQuery, params);
        return this.template(model).queryForList("select * from " + this.from(model) + where, params).stream()
                .map(row -> {
                    Map<String, Object> normalized = this.normalizeRow(model, row);
                    Map<String, Object> map = new LinkedHashMap<>();
                    for (Column column : columns) map.put(column.getAlias(), normalized.get(column.getName()));
                    return map;
                }).collect(Collectors.toList());
    }

    public void insert(EruptModel model, Object bean) {
        String pk = model.getErupt().primaryKeyCol();
        Map<String, Object> values = this.beanToValues(model, bean);
        values.values().removeIf(Objects::isNull);
        if (values.isEmpty()) return;
        String sql = "insert into " + this.table(model) + " (" + String.join(", ", values.keySet()) + ") values ("
                + values.keySet().stream().map(it -> ":" + it).collect(Collectors.joining(", ")) + ")";
        if (values.containsKey(pk)) {
            this.template(model).update(sql, values);
            return;
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.template(model).update(sql, new MapSqlParameterSource(values), keyHolder);
        this.writeGeneratedKey(model, bean, pk, keyHolder);
    }

    public void update(EruptModel model, Object bean) {
        String pk = model.getErupt().primaryKeyCol();
        Map<String, Object> values = this.beanToValues(model, bean);
        if (null == values.get(pk)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("jdbc.primary_key_missing"));
        }
        String sql = "update " + this.table(model) + " set " + values.keySet().stream()
                .filter(it -> !it.equals(pk)).map(it -> it + " = :" + it).collect(Collectors.joining(", "))
                + " where " + pk + " = :" + pk;
        this.template(model).update(sql, values);
    }

    public void delete(EruptModel model, Object bean) {
        String pk = model.getErupt().primaryKeyCol();
        Object id = this.beanToValues(model, bean).get(pk);
        if (null == id) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("jdbc.primary_key_missing"));
        this.template(model).update("delete from " + this.table(model) + " where " + pk + " = :id",
                Collections.singletonMap("id", id));
    }

    // ---------------------------------------------------------------- hooks

    /**
     * SQL expression standing for a field in ORDER BY and default comparisons.
     */
    protected String column(EruptModel model, EruptFieldModel field) {
        return field.getFieldName();
    }

    /**
     * Custom SQL for one condition. Implementations bind their own values into {@code params}
     * using {@code param} as the name prefix. Return null to fall back to the default comparison.
     */
    protected String condition(EruptModel model, EruptFieldModel field, Condition condition, String param, Map<String, Object> params) {
        return null;
    }

    /**
     * Java field value → JDBC parameter. Non-scalar values are serialised to JSON text.
     */
    protected Object toColumn(EruptFieldModel field, Object value) {
        if (null == value) return null;
        if (value instanceof Enum<?> e) return e.name();
        if (this.scalar(value)) return value;
        return gson.toJson(value);
    }

    /**
     * JDBC column value → java field value, driven by the declared field type.
     */
    protected Object fromColumn(EruptFieldModel field, Object value) {
        if (null == value || null == field.getField()) return value;
        Class<?> type = field.getField().getType();
        if (type.isInstance(value)) return value;
        if (value instanceof String text && this.jsonType(type)) {
            return text.isEmpty() ? null : gson.fromJson(text, field.getField().getGenericType());
        }
        if (value instanceof Number number) {
            if (type == Long.class || type == long.class) return number.longValue();
            if (type == Integer.class || type == int.class) return number.intValue();
            if (type == Double.class || type == double.class) return number.doubleValue();
            if (type == Float.class || type == float.class) return number.floatValue();
            if (type == Short.class || type == short.class) return number.shortValue();
            if (type == Boolean.class || type == boolean.class) return number.intValue() != 0;
            if (type == BigDecimal.class) return new BigDecimal(number.toString());
            if (type == Date.class) return new Date(number.longValue());
        }
        if (value instanceof Timestamp timestamp) {
            if (type == LocalDateTime.class) return timestamp.toLocalDateTime();
            if (type == LocalDate.class) return timestamp.toLocalDateTime().toLocalDate();
        }
        if (value instanceof java.sql.Date date) {
            if (type == LocalDate.class) return date.toLocalDate();
            if (type == LocalDateTime.class) return date.toLocalDate().atStartOfDay();
        }
        if (value instanceof Time time && type == LocalTime.class) return time.toLocalTime();
        if (value instanceof String text && this.temporal(type)) return this.parseTemporal(type, text);
        return TypeUtil.typeStrConvertObject(value, type);
    }

    // ---------------------------------------------------------------- internals

    protected NamedParameterJdbcTemplate template(EruptModel model) {
        return templates.apply(model);
    }

    protected String table(EruptModel model) {
        return tables.apply(model);
    }

    // alias the table with the erupt name so "Entity.field" condition strings (drill / @Filter) resolve
    protected String from(EruptModel model) {
        return this.table(model) + " " + model.getEruptName();
    }

    protected String where(EruptModel model, EruptQuery eruptQuery, Map<String, Object> params) {
        List<String> segments = new ArrayList<>();
        int index = 0;
        for (Condition condition : Optional.ofNullable(eruptQuery.getConditions()).orElse(Collections.emptyList())) {
            EruptFieldModel field = this.legalField(model, condition.getKey());
            String p = "p" + index++;
            String custom = this.condition(model, field, condition, p, params);
            if (null != custom) {
                segments.add(custom);
                continue;
            }
            String key = this.column(model, field);
            Object value = this.convertTarget(field, condition.getValue());
            switch (condition.getExpression()) {
                case EQ -> this.bind(segments, params, key + " = :" + p, p, value);
                case NEQ -> this.bind(segments, params, key + " <> :" + p, p, value);
                case GT -> this.bind(segments, params, key + " > :" + p, p, value);
                case GTE -> this.bind(segments, params, key + " >= :" + p, p, value);
                case LT -> this.bind(segments, params, key + " < :" + p, p, value);
                case LTE -> this.bind(segments, params, key + " <= :" + p, p, value);
                case LIKE -> this.bind(segments, params, key + " like :" + p, p, "%" + condition.getValue() + "%");
                case NOT_LIKE -> this.bind(segments, params, key + " not like :" + p, p, "%" + condition.getValue() + "%");
                case RANGE -> {
                    List<?> range = (List<?>) value;
                    segments.add(key + " between :" + p + "_a and :" + p + "_b");
                    params.put(p + "_a", range.get(0));
                    params.put(p + "_b", range.get(1));
                }
                case IN -> this.bind(segments, params, key + " in (:" + p + ")", p, value);
                case NOT_IN -> this.bind(segments, params, key + " not in (:" + p + ")", p, value);
                case NULL -> segments.add(key + " is null");
                case NOT_NULL -> segments.add(key + " is not null");
                default -> {
                }
            }
        }
        // condition strings originate from server-side annotations (drill / @Filter), not client input
        Optional.ofNullable(eruptQuery.getConditionStrings()).ifPresent(strings ->
                strings.stream().filter(it -> null != it && !it.trim().isEmpty()).forEach(segments::add));
        return segments.isEmpty() ? "" : " where " + String.join(" and ", segments);
    }

    private void bind(List<String> segments, Map<String, Object> params, String segment, String param, Object value) {
        segments.add(segment);
        params.put(param, value);
    }

    protected EruptFieldModel legalField(EruptModel model, String key) {
        EruptFieldModel field = model.getEruptFieldMap().get(key);
        if (null == field) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("jdbc.illegal_field") + " → " + key);
        }
        return field;
    }

    /**
     * Client-supplied condition value → the java type of the field, so the driver binds a
     * comparable parameter (numbers as numbers, dates as timestamps).
     */
    protected Object convertTarget(EruptFieldModel field, Object value) {
        if (null == field || null == field.getField() || null == value) return value;
        Class<?> type = field.getField().getType();
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(it -> this.convertTarget(field, it)).collect(Collectors.toList());
        }
        if (type.isInstance(value)) return value;
        if (this.temporal(type) && value instanceof String text) return this.parseTemporal(type, text);
        if (this.jsonType(type)) return value;
        return TypeUtil.typeStrConvertObject(value, type);
    }

    private String orderBy(EruptModel model, Page page) {
        List<Sort> sorts = null == page.getSort() || page.getSort().isEmpty()
                ? Sort.toSortList(model.getErupt().orderBy()) : page.getSort();
        if (sorts.isEmpty()) return "";
        return " order by " + sorts.stream()
                .map(sort -> this.column(model, this.legalField(model, sort.getField()))
                        + (sort.getDirection() == Direction.DESC ? " desc" : " asc"))
                .collect(Collectors.joining(", "));
    }

    // JDBC drivers report column labels in driver-specific case (e.g. H2 upper-cases them); remap
    // through the case-insensitive result row so keys match java field names exactly, convert
    // column values to field types and flatten Map-valued fields into field_key entries
    protected Map<String, Object> normalizeRow(EruptModel model, Map<String, Object> row) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (EruptFieldModel field : model.getEruptFieldModels()) {
            if (!row.containsKey(field.getFieldName())) continue;
            Object value = this.fromColumn(field, row.get(field.getFieldName()));
            map.put(field.getFieldName(), value);
            if (value instanceof Map<?, ?> nested) {
                nested.forEach((k, v) -> map.put(field.getFieldName() + "_" + k, v));
            }
        }
        String pk = model.getErupt().primaryKeyCol();
        if (!map.containsKey(pk) && row.containsKey(pk)) map.put(pk, row.get(pk));
        return map;
    }

    @SneakyThrows
    protected Object toBean(EruptModel model, Map<String, Object> row) {
        Object bean = model.getClazz().getDeclaredConstructor().newInstance();
        for (EruptFieldModel fieldModel : model.getEruptFieldModels()) {
            Object value = row.get(fieldModel.getFieldName());
            if (null == value) continue;
            Field field = fieldModel.getField();
            field.setAccessible(true);
            field.set(bean, this.fromColumn(fieldModel, value));
        }
        return bean;
    }

    @SneakyThrows
    protected Map<String, Object> beanToValues(EruptModel model, Object bean) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (EruptFieldModel fieldModel : model.getEruptFieldModels()) {
            Field field = fieldModel.getField();
            field.setAccessible(true);
            values.put(fieldModel.getFieldName(), this.toColumn(fieldModel, field.get(bean)));
        }
        return values;
    }

    // drivers either return just the generated key (SQLite: last_insert_rowid()) or the whole row (PostgreSQL)
    @SneakyThrows
    private void writeGeneratedKey(EruptModel model, Object bean, String pk, KeyHolder keyHolder) {
        EruptFieldModel pkField = model.getEruptFieldMap().get(pk);
        if (null == pkField || null == pkField.getField() || keyHolder.getKeyList().isEmpty()) return;
        Map<String, Object> keys = keyHolder.getKeyList().get(0);
        Object key = keys.entrySet().stream().filter(it -> it.getKey().equalsIgnoreCase(pk))
                .map(Map.Entry::getValue).findFirst()
                .orElseGet(() -> keys.size() == 1 ? keys.values().iterator().next() : null);
        if (null == key) return;
        Field field = pkField.getField();
        field.setAccessible(true);
        field.set(bean, this.fromColumn(pkField, key));
    }

    private boolean scalar(Object value) {
        return value instanceof CharSequence || value instanceof Number || value instanceof Boolean
                || value instanceof Character || value instanceof Date || value instanceof Temporal
                || value instanceof byte[];
    }

    // field types that are stored as JSON text rather than a native column value
    protected boolean jsonType(Class<?> type) {
        return Map.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type)
                || type.isArray() && type != byte[].class
                || !(type.isPrimitive() || type.isEnum() || Number.class.isAssignableFrom(type)
                || CharSequence.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class
                || this.temporal(type) || type == byte[].class || type == Object.class);
    }

    private boolean temporal(Class<?> type) {
        return Date.class.isAssignableFrom(type) || Temporal.class.isAssignableFrom(type);
    }

    @SneakyThrows
    private Object parseTemporal(Class<?> type, String text) {
        if (type == Date.class) return DateUtil.parseDate(text);
        return DateUtil.getDate(type, text);
    }

}
