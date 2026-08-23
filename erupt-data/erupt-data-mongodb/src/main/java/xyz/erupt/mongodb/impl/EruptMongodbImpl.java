package xyz.erupt.mongodb.impl;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Direction;
import xyz.erupt.core.exception.EruptFieldAnnotationException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.util.TypeUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author YuePeng
 * date 2020-03-06.
 */
@Service
public class EruptMongodbImpl implements IEruptDataService, ApplicationRunner {

    public static final String MONGODB_PROCESS = "mongodb";

    @Resource
    private MongoTemplate mongoTemplate;

    private static final Map<Class<?>, Map<String, String>> MODEL_CLASS_FIELD_MAPPING = new ConcurrentHashMap<>(16);

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        Query query = new Query(Criteria.where(eruptModel.getErupt().primaryKeyCol()).is(id));
        return mongoTemplate.findOne(query, eruptModel.getClazz());
    }

    @SneakyThrows
    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        Query query = new Query();
        this.addQueryCondition(eruptModel, eruptQuery, query);
        page.setTotal(mongoTemplate.count(query, eruptModel.getClazz()));
        if (page.getTotal() > 0) {
            query.limit(page.getPageSize());
            query.skip((long) (page.getPageIndex() - 1) * page.getPageSize());
            if (null != page.getSort() && !page.getSort().isEmpty()) {
                for (xyz.erupt.annotation.query.Sort sort : page.getSort()) {
                    String mongoFieldName = this.populateMapping(eruptModel, sort.getField());
                    query.with(Sort.by(sort.getDirection() == Direction.DESC
                            ? Sort.Direction.DESC : Sort.Direction.ASC, mongoFieldName));
                }
            } else if (!"".equals(eruptModel.getErupt().orderBy())) {
                this.orderByTokenToQuery(eruptModel, query, eruptModel.getErupt().orderBy());
            }
            List<Map<String, Object>> newList = new ArrayList<>();
            for (Object obj : mongoTemplate.find(query, eruptModel.getClazz())) {
                newList.add(mongoObjectToMap(obj));
            }
            page.setList(newList);
        } else {
            page.setList(new ArrayList<>());
        }
        return page;
    }

    private void orderByTokenToQuery(EruptModel eruptModel, Query query, String orderByStr) {
        for (String s : orderByStr.split(",")) {
            String[] orderBy = s.split(" ");
            String orderByFieldName = orderBy[0];
            String mongoFieldName = this.populateMapping(eruptModel, orderByFieldName);
            if (orderBy.length > 1 && orderBy[1].contains("desc")) {
                query.with(Sort.by(Sort.Direction.DESC, mongoFieldName));
            } else {
                query.with(Sort.by(Sort.Direction.ASC, mongoFieldName));
            }
        }
    }

    public void addQueryCondition(EruptModel eruptModel, EruptQuery eruptQuery, Query query) {
        for (Condition condition : Optional.ofNullable(eruptQuery.getConditions()).orElse(Collections.emptyList())) {
            String conditionKey = condition.getKey();
            EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(conditionKey);
            String mongoFieldName = this.populateMapping(eruptModel, conditionKey);
            // NULL / NOT_NULL carry no value, so they must be applied before value conversion;
            // {field: null} also matches missing fields, mirroring "is null" in the other impls
            if (QueryExpression.NULL == condition.getExpression()) {
                query.addCriteria(Criteria.where(mongoFieldName).is(null));
                continue;
            }
            if (QueryExpression.NOT_NULL == condition.getExpression()) {
                query.addCriteria(Criteria.where(mongoFieldName).ne(null));
                continue;
            }
            Optional.ofNullable(this.convertConditionValue(condition, eruptFieldModel)).ifPresent(value -> {
                switch (condition.getExpression()) {
                    case EQ -> query.addCriteria(Criteria.where(mongoFieldName).is(value));
                    case NEQ -> query.addCriteria(Criteria.where(mongoFieldName).ne(value));
                    case GT -> query.addCriteria(Criteria.where(mongoFieldName).gt(value));
                    case GTE -> query.addCriteria(Criteria.where(mongoFieldName).gte(value));
                    case LT -> query.addCriteria(Criteria.where(mongoFieldName).lt(value));
                    case LTE -> query.addCriteria(Criteria.where(mongoFieldName).lte(value));
                    case LIKE -> query.addCriteria(Criteria.where(mongoFieldName)
                            .regex("^.*" + Pattern.quote(String.valueOf(value)) + ".*$"));
                    case NOT_LIKE -> query.addCriteria(Criteria.where(mongoFieldName)
                            .not().regex("^.*" + Pattern.quote(String.valueOf(value)) + ".*$"));
                    case RANGE -> {
                        List<?> list = (List<?>) value;
                        query.addCriteria(Criteria.where(mongoFieldName).gte(list.get(0)).lte(list.get(1)));
                    }
                    case IN -> query.addCriteria(value instanceof Collection<?> collection
                            ? Criteria.where(mongoFieldName).in(collection)
                            : Criteria.where(mongoFieldName).in(value));
                    case NOT_IN -> query.addCriteria(value instanceof Collection<?> collection
                            ? Criteria.where(mongoFieldName).nin(collection)
                            : Criteria.where(mongoFieldName).nin(value));
                    default -> {
                    }
                }
            });
        }
        // Parse drill / filter condition strings: "Entity.field = value" or "Entity.field = 'value'"
        if (eruptQuery.getConditionStrings() != null) {
            for (String cs : eruptQuery.getConditionStrings()) {
                applyConditionString(query, cs);
            }
        }
    }

    // Parses simple "Entity.field = value" / "Entity.field = 'value'" equality conditions
    // produced by drillProcess and static @Filter values into MongoDB Criteria.
    private void applyConditionString(Query query, String conditionStr) {
        if (StringUtils.isBlank(conditionStr)) return;
        String trimmed = conditionStr.trim();
        int eqIdx = trimmed.indexOf('=');
        if (eqIdx < 0) return;
        String lhs = trimmed.substring(0, eqIdx).trim();
        String rhs = trimmed.substring(eqIdx + 1).trim();
        // strip Entity. prefix (e.g. "DrillDetailModel.orderId" → "orderId")
        int dotIdx = lhs.lastIndexOf('.');
        String fieldName = dotIdx >= 0 ? lhs.substring(dotIdx + 1) : lhs;
        if (StringUtils.isBlank(fieldName)) return;
        Object value;
        if (rhs.startsWith("'") && rhs.endsWith("'")) {
            value = rhs.substring(1, rhs.length() - 1);
        } else {
            try {
                value = Long.parseLong(rhs);
            } catch (NumberFormatException e1) {
                try {
                    value = Double.parseDouble(rhs);
                } catch (NumberFormatException e2) {
                    value = rhs;
                }
            }
        }
        query.addCriteria(Criteria.where(fieldName).is(value));
    }

    /**
     * <note>Due to strict type checking in MongoDB</note>
     * Convert the query condition parameter value type based on the field type annotated by {@link xyz.erupt.annotation.EruptField}
     */
    protected Object convertConditionValue(Condition condition, EruptFieldModel eruptFieldModel) {
        Object value = condition.getValue();
        // keys outside the model (e.g. dotted association paths) have no field type to align with
        if (null == eruptFieldModel || null == eruptFieldModel.getField() || null == value) return value;
        if (condition.getValue() instanceof Collection) {
            Collection<?> listValue = (Collection<?>) value;
            List<Object> objects = new ArrayList<>(listValue.size());
            for (Object object : listValue) {
                objects.add(TypeUtil.typeStrConvertObject(object, eruptFieldModel.getField().getType()));
            }
            value = objects;
        } else {
            value = TypeUtil.typeStrConvertObject(value, eruptFieldModel.getField().getType());
        }
        return value;
    }

    protected String populateMapping(EruptModel eruptModel, String fieldName) {
        Map<String, String> eruptFieldMongFieldMap = MODEL_CLASS_FIELD_MAPPING.getOrDefault(eruptModel.getClazz(), new HashMap<>());
        String mongoField = eruptFieldMongFieldMap.get(fieldName);
        if (StringUtils.isNotBlank(mongoField)) {
            return mongoField;
        }

        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        if (null == eruptFieldModel) {
            return fieldName;
        }
        Field eruptFieldModelField = eruptFieldModel.getField();
        org.springframework.data.mongodb.core.mapping.Field mongoFieldAnnotation = eruptFieldModelField.getAnnotation(org.springframework.data.mongodb.core.mapping.Field.class);
        // no @Field mapping declared: the java field name is the mongo field name
        if (null == mongoFieldAnnotation) {
            return fieldName;
        }
        mongoField = Optional.of(mongoFieldAnnotation).map(obj -> StringUtils.defaultIfBlank(obj.value(), eruptFieldModelField.getName())).orElseThrow(() -> new EruptFieldAnnotationException("There is an error in the MongoDB field mapping configuration."));
        eruptFieldMongFieldMap.put(fieldName, mongoField);
        MODEL_CLASS_FIELD_MAPPING.put(eruptModel.getClazz(), eruptFieldMongFieldMap);
        return mongoField;
    }


    @SneakyThrows
    private Map<String, Object> mongoObjectToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        for (Class<?> clazz = obj.getClass(); null != clazz && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                map.putIfAbsent(field.getName(), field.get(obj));
            }
        }
        return map;
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        mongoTemplate.insert(object);
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        mongoTemplate.save(object);
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        mongoTemplate.remove(object);
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        Query query = new Query();
        this.addQueryCondition(eruptModel, eruptQuery, query);
        columns.stream().map(column -> this.populateMapping(eruptModel, column.getName())).forEach(query.fields()::include);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object obj : mongoTemplate.find(query, eruptModel.getClazz())) {
            Map<String, Object> row = this.mongoObjectToMap(obj);
            // project by column alias, matching the jpa / memory implementations
            Map<String, Object> map = new LinkedHashMap<>();
            for (Column column : columns) map.put(column.getAlias(), row.get(column.getName()));
            list.add(map);
        }
        return list;
    }

    @Override
    public void run(ApplicationArguments args) {
        DataProcessorManager.register(MONGODB_PROCESS, EruptMongodbImpl.class);
    }
}
