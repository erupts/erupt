package xyz.erupt.mongodb.impl;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.Condition;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author YuePeng
 * date 2020-03-06.
 */
@Service
public class EruptMongodbImpl implements IEruptDataService, ApplicationRunner {

    public static final String MONGODB_PROCESS = "mongodb";

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        Query query = new Query(Criteria.where(eruptModel.getErupt().primaryKeyCol()).is(id));
        return mongoTemplate.findOne(query, eruptModel.getClazz());
    }

    @SneakyThrows
    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        Query query = new Query();
        this.addQueryCondition(eruptQuery, query);
        page.setTotal(mongoTemplate.count(query, eruptModel.getClazz()));
        if (page.getTotal() > 0) {
            query.limit(page.getPageSize());
            query.skip((page.getPageIndex() - 1) * page.getPageSize());
//            排序
            if (StringUtils.isNotBlank(page.getSort())) {
                for (String s : page.getSort().split(",")) {
                    if (s.split(" ")[1].contains("desc")) {
                        query.with(Sort.by(Sort.Direction.DESC, s.split(" ")[0]));
                    } else {
                        query.with(Sort.by(Sort.Direction.ASC, s.split(" ")[0]));
                    }
                }
            }
            List<Map<String, Object>> newList = new ArrayList<>();
            for (Object obj : mongoTemplate.find(query, eruptModel.getClazz())) {
                newList.add(mongoObjectToMap(obj));
            }
            page.setList(newList);
        }
        return page;
    }

    public void addQueryCondition(EruptQuery eruptQuery, Query query) {
        for (Condition condition : eruptQuery.getConditions()) {
            switch (condition.getExpression()) {
                case EQ:
                    query.addCriteria(Criteria.where(condition.getKey()).is(condition.getValue()));
                    break;
                case LIKE:
                    query.addCriteria(Criteria.where(condition.getKey()).regex("^.*" + condition.getValue() + ".*$"));
                    break;
                case RANGE:
                    List<?> list = (List<?>) condition.getValue();
                    query.addCriteria(Criteria.where(condition.getKey()).gte(list.get(0)).lte(list.get(1)));
                    break;
                case IN:
                    query.addCriteria(Criteria.where(condition.getKey()).in(condition.getValue()));
                    break;
            }
        }
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
        this.addQueryCondition(eruptQuery, query);
        columns.stream().map(Column::getName).forEach(query.fields()::include);
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
