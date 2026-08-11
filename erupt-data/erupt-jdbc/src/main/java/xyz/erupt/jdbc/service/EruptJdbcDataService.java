package xyz.erupt.jdbc.service;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Direction;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.util.TypeUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.jdbc.annotation.EruptJdbc;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Plain JDBC data source: models annotated with {@link EruptJdbc} are mapped to a
 * single table, with filtering, sorting and paging pushed down as SQL. Condition
 * values are always bound as named parameters; condition keys and sort fields must
 * be declared model fields — both guard against SQL injection.
 * <p>
 * The table is aliased with the erupt class name so drill / {@code @Filter}
 * condition strings ("Entity.field = 'x'") work verbatim, as in the JPA impl.
 *
 * @author YuePeng
 */
@Service
public class EruptJdbcDataService implements IEruptDataService {

    public static final String DATA_PROCESSOR = "jdbc";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptJdbcDataService.class);
    }

    @Resource
    private ApplicationContext applicationContext;

    private final Map<String, NamedParameterJdbcTemplate> templates = new ConcurrentHashMap<>();

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        EruptJdbc eruptJdbc = this.eruptJdbc(eruptModel);
        String pk = eruptModel.getErupt().primaryKeyCol();
        List<Map<String, Object>> rows = this.template(eruptJdbc).queryForList(
                "select * from " + this.from(eruptModel, eruptJdbc) + " where " + pk + " = :id",
                Collections.singletonMap("id", id));
        return rows.isEmpty() ? null : this.toBean(eruptModel, rows.get(0));
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        EruptJdbc eruptJdbc = this.eruptJdbc(eruptModel);
        Map<String, Object> params = new HashMap<>();
        String where = this.where(eruptModel, eruptQuery, params);
        NamedParameterJdbcTemplate template = this.template(eruptJdbc);
        page.setTotal(template.queryForObject(
                "select count(*) from " + this.from(eruptModel, eruptJdbc) + where, params, Long.class));
        String sql = "select * from " + this.from(eruptModel, eruptJdbc) + where + this.orderBy(eruptModel, page)
                + " limit " + page.getPageSize() + " offset " + (page.getPageIndex() - 1) * page.getPageSize();
        page.setList(template.queryForList(sql, params).stream()
                .map(row -> this.normalizeRow(eruptModel, row)).collect(Collectors.toList()));
        return page;
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        EruptJdbc eruptJdbc = this.eruptJdbc(eruptModel);
        Map<String, Object> params = new HashMap<>();
        String where = this.where(eruptModel, eruptQuery, params);
        return this.template(eruptJdbc)
                .queryForList("select * from " + this.from(eruptModel, eruptJdbc) + where, params).stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    for (Column column : columns) map.put(column.getAlias(), row.get(column.getName()));
                    return map;
                }).collect(Collectors.toList());
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        EruptJdbc eruptJdbc = this.eruptJdbc(eruptModel);
        Map<String, Object> values = this.beanToValues(eruptModel, object);
        values.values().removeIf(Objects::isNull);
        if (values.isEmpty()) return;
        String sql = "insert into " + eruptJdbc.value() + " (" + String.join(", ", values.keySet()) + ") values ("
                + values.keySet().stream().map(it -> ":" + it).collect(Collectors.joining(", ")) + ")";
        this.template(eruptJdbc).update(sql, values);
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        EruptJdbc eruptJdbc = this.eruptJdbc(eruptModel);
        String pk = eruptModel.getErupt().primaryKeyCol();
        Map<String, Object> values = this.beanToValues(eruptModel, object);
        if (null == values.get(pk)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("jdbc.primary_key_missing"));
        }
        String sql = "update " + eruptJdbc.value() + " set " + values.keySet().stream()
                .filter(it -> !it.equals(pk)).map(it -> it + " = :" + it).collect(Collectors.joining(", "))
                + " where " + pk + " = :" + pk;
        this.template(eruptJdbc).update(sql, values);
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        EruptJdbc eruptJdbc = this.eruptJdbc(eruptModel);
        String pk = eruptModel.getErupt().primaryKeyCol();
        Object id = this.beanToValues(eruptModel, object).get(pk);
        if (null == id) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("jdbc.primary_key_missing"));
        this.template(eruptJdbc).update("delete from " + eruptJdbc.value() + " where " + pk + " = :id",
                Collections.singletonMap("id", id));
    }

    private EruptJdbc eruptJdbc(EruptModel eruptModel) {
        EruptJdbc eruptJdbc = eruptModel.getClazz().getAnnotation(EruptJdbc.class);
        if (null == eruptJdbc) {
            throw new EruptWebApiRuntimeException("@EruptJdbc annotation is missing on " + eruptModel.getEruptName());
        }
        return eruptJdbc;
    }

    private NamedParameterJdbcTemplate template(EruptJdbc eruptJdbc) {
        return templates.computeIfAbsent(eruptJdbc.datasource(), name -> new NamedParameterJdbcTemplate(
                name.isEmpty() ? applicationContext.getBean(DataSource.class)
                        : applicationContext.getBean(name, DataSource.class)));
    }

    // Alias the table with the erupt name so "Entity.field" condition strings resolve
    private String from(EruptModel eruptModel, EruptJdbc eruptJdbc) {
        return eruptJdbc.value() + " " + eruptModel.getEruptName();
    }

    private String where(EruptModel eruptModel, EruptQuery eruptQuery, Map<String, Object> params) {
        List<String> segments = new ArrayList<>();
        int index = 0;
        for (Condition condition : Optional.ofNullable(eruptQuery.getConditions()).orElse(Collections.emptyList())) {
            String key = this.legalField(eruptModel, condition.getKey());
            String p = "p" + index++;
            Object value = this.convertTarget(eruptModel, key, condition.getValue());
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

    private String legalField(EruptModel eruptModel, String key) {
        if (!eruptModel.getEruptFieldMap().containsKey(key)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("jdbc.illegal_field") + " → " + key);
        }
        return key;
    }

    private Object convertTarget(EruptModel eruptModel, String key, Object value) {
        EruptFieldModel fieldModel = eruptModel.getEruptFieldMap().get(key);
        if (null == fieldModel || null == fieldModel.getField() || null == value) return value;
        Class<?> type = fieldModel.getField().getType();
        if (value instanceof Collection) {
            return ((Collection<?>) value).stream()
                    .map(it -> TypeUtil.typeStrConvertObject(it, type)).collect(Collectors.toList());
        }
        return TypeUtil.typeStrConvertObject(value, type);
    }

    private String orderBy(EruptModel eruptModel, Page page) {
        List<Sort> sorts = null == page.getSort() || page.getSort().isEmpty()
                ? Sort.toSortList(eruptModel.getErupt().orderBy()) : page.getSort();
        if (sorts.isEmpty()) return "";
        return " order by " + sorts.stream()
                .map(sort -> this.legalField(eruptModel, sort.getField())
                        + (sort.getDirection() == Direction.DESC ? " desc" : " asc"))
                .collect(Collectors.joining(", "));
    }

    // JDBC drivers report column labels in driver-specific case (e.g. H2 upper-cases them);
    // remap through the case-insensitive result row so keys match java field names exactly
    private Map<String, Object> normalizeRow(EruptModel eruptModel, Map<String, Object> row) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            if (row.containsKey(fieldModel.getFieldName())) {
                map.put(fieldModel.getFieldName(), row.get(fieldModel.getFieldName()));
            }
        }
        String pk = eruptModel.getErupt().primaryKeyCol();
        if (!map.containsKey(pk) && row.containsKey(pk)) map.put(pk, row.get(pk));
        return map;
    }

    @SneakyThrows
    private Object toBean(EruptModel eruptModel, Map<String, Object> row) {
        Object bean = eruptModel.getClazz().getDeclaredConstructor().newInstance();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            Object value = row.get(fieldModel.getFieldName());
            if (null == value) continue;
            Field field = fieldModel.getField();
            field.setAccessible(true);
            field.set(bean, this.convertColumnValue(value, field.getType()));
        }
        return bean;
    }

    private Object convertColumnValue(Object value, Class<?> type) {
        if (type.isInstance(value)) return value;
        if (value instanceof Number number) {
            if (type == Long.class || type == long.class) return number.longValue();
            if (type == Integer.class || type == int.class) return number.intValue();
            if (type == Double.class || type == double.class) return number.doubleValue();
            if (type == Float.class || type == float.class) return number.floatValue();
            if (type == Short.class || type == short.class) return number.shortValue();
            if (type == Boolean.class || type == boolean.class) return number.intValue() != 0;
            if (type == BigDecimal.class) return new BigDecimal(number.toString());
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
        return TypeUtil.typeStrConvertObject(value, type);
    }

    @SneakyThrows
    private Map<String, Object> beanToValues(EruptModel eruptModel, Object object) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            Field field = fieldModel.getField();
            field.setAccessible(true);
            values.put(fieldModel.getFieldName(), field.get(object));
        }
        return values;
    }

}
