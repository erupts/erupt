package xyz.erupt.es.impl;

import lombok.SneakyThrows;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2020-03-06.
 */
@Service
public class EruptElasticSearchImpl implements IEruptDataService {

    public static final String ELASTICSEARCH_PROCESS = "es";

    static {
        DataProcessorManager.register(ELASTICSEARCH_PROCESS, EruptElasticSearchImpl.class);
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
//                .withQuery(QueryBuilders.multiMatchQuery("哈哈哈哈", "content", "type"))
//                .withQuery(QueryBuilders.matchQuery("content", "ははab"))
//                .withQuery(QueryBuilders.matchPhrasePrefixQuery("str", "abc"))
//                .withQuery(QueryBuilders.rangeQuery("date").lte(new Date()));
        page.setTotal(elasticsearchRestTemplate.count(nativeSearchQuery.build(), eruptModel.getClazz()));
//        nativeSearchQuery.withSorts(SortBuilder.);
        nativeSearchQuery.withPageable(PageRequest.of(page.getPageIndex(), page.getPageSize()));
        SearchHits<?> search = elasticsearchRestTemplate.search(nativeSearchQuery.build(), eruptModel.getClazz());
        return page;
    }

//    public void addQueryCondition(EruptQuery eruptQuery, QueryWrapper queryWrapper) {
//        for (Condition condition : eruptQuery.getConditions()) {
//            switch (condition.getExpression()) {
//                case EQ:
//
//                    break;
//                case LIKE:
//
//                    break;
//                case RANGE:
//
//                    break;
//                case IN:
//
//                    break;
//            }
//        }
//    }


    @Override
    public void addData(EruptModel eruptModel, Object object) {
        elasticsearchRestTemplate.save(object);
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        Document document = Document.parse(GsonFactory.getGson().toJson(object));
        elasticsearchRestTemplate.indexOps(eruptModel.getClazz()).putMapping(document);
//        UpdateQuery query = UpdateQuery
//                .builder(String.valueOf(updateId))
//                .withDocument(document)
//                .build();
//        IndexCoordinates indexCoordinates = elasticsearchRestTemplate.getIndexCoordinatesFor(eruptModel.getClazz());
//        elasticsearchRestTemplate.update(updateQuery, indexCoordinates);
//        elasticsearchRestTemplate.update(updateQuery, object);
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        elasticsearchRestTemplate.delete(object);
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
//        elasticsearchRestTemplate.
        return null;
    }

}
