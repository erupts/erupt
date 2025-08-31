package xyz.erupt.es.process;

import lombok.SneakyThrows;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import jakarta.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author YuePeng
 * date 2020-03-06.
 */
@Service
public class EruptElasticSearchProcess implements IEruptDataService {

    public static final String ELASTICSEARCH_PROCESS = "es";

    static {
        DataProcessorManager.register(ELASTICSEARCH_PROCESS, EruptElasticSearchProcess.class);
    }

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        return elasticsearchRestTemplate.get(id.toString(), eruptModel.getClazz());
    }

    @SneakyThrows
    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        this.addQueryCondition(eruptQuery, nativeSearchQuery);
//                .withQuery(QueryBuilders.multiMatchQuery("哈哈哈哈", "content", "type"))
//                .withQuery(QueryBuilders.matchQuery("content", "ははab"))
//                .withQuery(QueryBuilders.matchPhrasePrefixQuery("str", "abc"))
//                .withQuery(QueryBuilders.rangeQuery("date").lte(new Date()));
        page.setTotal(elasticsearchRestTemplate.count(nativeSearchQuery.build(), eruptModel.getClazz()));
//        nativeSearchQuery.withSorts(SortBuilder.);
        nativeSearchQuery.withPageable(PageRequest.of(page.getPageIndex() - 1, page.getPageSize()));
        SearchHits<?> search = elasticsearchRestTemplate.search(nativeSearchQuery.build(), eruptModel.getClazz());
        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit<?> hit : search.getSearchHits()) {
            list.add(objectToMap(hit.getContent()));
        }
        page.setList(list);
        return page;
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        List<String> fields = new ArrayList<>(columns.size());
        for (Column column : columns) {
            fields.add(column.getName());
        }
        nativeSearchQuery.withFields(fields);
        this.addQueryCondition(eruptQuery, nativeSearchQuery);
        List<Map<String, Object>> list = new ArrayList<>();
        SearchHits<?> search = elasticsearchRestTemplate.search(nativeSearchQuery.build(), eruptModel.getClazz());
        for (SearchHit<?> hit : search.getSearchHits()) {
            list.add(objectToMap(hit.getContent()));
        }
        return list;
    }

    @SneakyThrows
    private Map<String, Object> objectToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
        }
        return map;
    }

    private void addQueryCondition(EruptQuery eruptQuery, NativeSearchQueryBuilder nativeSearchQuery) {
        for (Condition condition : eruptQuery.getConditions()) {
            switch (condition.getExpression()) {
                case EQ:
                    nativeSearchQuery.withQuery(QueryBuilders.matchQuery(condition.getKey(), condition.getValue().toString()));
                    break;
                case RANGE:
                    nativeSearchQuery.withQuery(QueryBuilders.rangeQuery(condition.getKey())
                            .lte(condition.getValue())
                            .gte(condition.getValue())
                    );
                    break;
                case IN:
//                    nativeSearchQuery.with(QueryBuilders. (condition.getKey()));
                    break;
            }
        }
        for (String string : eruptQuery.getConditionStrings()) {
            nativeSearchQuery.withQuery(QueryBuilders.queryStringQuery(string));
        }
    }


    @Override
    public void addData(EruptModel eruptModel, Object object) {
        elasticsearchRestTemplate.save(object);
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        Document document = Document.parse(GsonFactory.getGson().toJson(object));
        elasticsearchRestTemplate.indexOps(eruptModel.getClazz()).putMapping(document);
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        elasticsearchRestTemplate.delete(object);
    }

}
