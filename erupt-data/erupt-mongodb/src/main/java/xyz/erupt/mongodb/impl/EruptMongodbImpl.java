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
import xyz.erupt.annotation.query.Condition;
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
                    if (sort.getDirection() == xyz.erupt.annotation.query.Sort.Direction.asc) {
                        query.with(Sort.by(Sort.Direction.ASC, sort.getField()));
                    } else {
                        query.with(Sort.by(Sort.Direction.DESC, sort.getField()));
                    }
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
        for (Condition condition : eruptQuery.getConditions()) {
            String conditionKey = condition.getKey();
            EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(conditionKey);
            String mongoFieldName = this.populateMapping(eruptModel, conditionKey);
            Optional.ofNullable(this.convertConditionValue(condition, eruptFieldModel)).ifPresent(value -> {
                switch (condition.getExpression()) {
                    case EQ:
                        query.addCriteria(Criteria.where(mongoFieldName).is(value));
                        break;
                    case LIKE:
                        query.addCriteria(Criteria.where(mongoFieldName).regex("^.*" + value + ".*$"));
                        break;
                    case RANGE:
                        List<?> list = (List<?>) value;
                        query.addCriteria(Criteria.where(mongoFieldName).gte(list.get(0)).lte(list.get(1)));
                        break;
                    case IN:
                        // 类型强制转换.
                        if (value instanceof Collection<?>) {
                            query.addCriteria(Criteria.where(mongoFieldName).in((Collection<?>) value));
                        } else {
                            query.addCriteria(Criteria.where(mongoFieldName).in(value));
                        }
                        break;
                }
            });
        }
    }

    /**
     * <note>由于mongodb类型检查严格</note>
     * 根据{@link xyz.erupt.annotation.EruptField} 标注的字段类型 转换查询条件参数值类型
     */
    protected Object convertConditionValue(Condition condition, EruptFieldModel eruptFieldModel) {
        Object value = condition.getValue();
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
        if (null == mongoFieldAnnotation) {
            return mongoField;
        }
        mongoField = Optional.of(mongoFieldAnnotation).map(obj -> StringUtils.defaultIfBlank(obj.value(), eruptFieldModelField.getName())).orElseThrow(() -> new EruptFieldAnnotationException("mongodb字段映射配置错误"));
        eruptFieldMongFieldMap.put(fieldName, mongoField);
        MODEL_CLASS_FIELD_MAPPING.put(eruptModel.getClazz(), eruptFieldMongFieldMap);
        return mongoField;
    }


    @SneakyThrows
    private Map<String, Object> mongoObjectToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
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
            list.add(mongoObjectToMap(obj));
        }
        return list;
    }

    @Override
    public void run(ApplicationArguments args) {
        DataProcessorManager.register(MONGODB_PROCESS, EruptMongodbImpl.class);
    }
}
