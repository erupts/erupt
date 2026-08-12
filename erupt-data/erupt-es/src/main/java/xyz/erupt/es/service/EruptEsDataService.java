package xyz.erupt.es.service;

import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Direction;
import xyz.erupt.annotation.query.Sort;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.es.annotation.EruptEs;
import xyz.erupt.memory.service.EruptMemoryDataService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch data source built on Spring Data Elasticsearch: the cluster is
 * configured with the standard {@code spring.elasticsearch.*} properties, and
 * conditions map to a {@link CriteriaQuery} so filtering, sorting and paging are
 * pushed down to {@code _search}. The in-memory base class contributes drill
 * condition-string parsing and bean → row conversion.
 * <p>
 * The document {@code _id} maps to the model's id property (named {@code id} or
 * annotated with Spring Data's {@code @Id}).
 *
 * @author YuePeng
 */
@Service
public class EruptEsDataService extends EruptMemoryDataService<Object> {

    public static final String DATA_PROCESSOR = "ELASTICSEARCH";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptEsDataService.class);
    }

    // ES rejects from + size beyond this window by default
    private static final int MAX_FETCH_SIZE = 10000;

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    protected List<Object> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        CriteriaQuery query = new CriteriaQuery(this.buildCriteria(eruptModel, eruptQuery));
        query.setMaxResults(MAX_FETCH_SIZE);
        return this.search(eruptModel, query).getSearchHits().stream()
                .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        CriteriaQuery query = new CriteriaQuery(this.buildCriteria(eruptModel, eruptQuery));
        query.setPageable(PageRequest.of(page.getPageIndex() - 1, page.getPageSize(), this.buildSort(eruptModel, page)));
        SearchHits<?> searchHits = this.search(eruptModel, query);
        page.setTotal(searchHits.getTotalHits());
        page.setList(searchHits.getSearchHits().stream()
                .map(hit -> this.toRow(eruptModel, hit.getContent())).collect(Collectors.toList()));
        return page;
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        return elasticsearchOperations.get(String.valueOf(id), eruptModel.getClazz(), this.index(eruptModel));
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        elasticsearchOperations.save(object, this.index(eruptModel));
        this.refresh(eruptModel);
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        this.requireId(eruptModel, object);
        elasticsearchOperations.save(object, this.index(eruptModel));
        this.refresh(eruptModel);
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        elasticsearchOperations.delete(String.valueOf(this.requireId(eruptModel, object)), this.index(eruptModel));
        this.refresh(eruptModel);
    }

    private Object requireId(EruptModel eruptModel, Object object) {
        Object id = this.readValue(eruptModel, object, eruptModel.getErupt().primaryKeyCol());
        if (null == id) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("es.primary_key_missing"));
        return id;
    }

    private SearchHits<?> search(EruptModel eruptModel, CriteriaQuery query) {
        return elasticsearchOperations.search(query, eruptModel.getClazz(), this.index(eruptModel));
    }

    // The admin table re-queries immediately after a write; refresh makes it visible
    private void refresh(EruptModel eruptModel) {
        elasticsearchOperations.indexOps(this.index(eruptModel)).refresh();
    }

    private IndexCoordinates index(EruptModel eruptModel) {
        EruptEs eruptEs = eruptModel.getClazz().getAnnotation(EruptEs.class);
        if (null == eruptEs) {
            throw new EruptWebApiRuntimeException("@EruptEs annotation is missing on " + eruptModel.getEruptName());
        }
        return IndexCoordinates.of(eruptEs.value());
    }

    private Criteria buildCriteria(EruptModel eruptModel, EruptQuery eruptQuery) {
        Criteria criteria = null;
        for (Condition condition : this.mergeConditions(eruptQuery)) {
            String key = condition.getKey();
            Object value = this.convertTarget(eruptModel, key, condition.getValue());
            Criteria sub = switch (condition.getExpression()) {
                case EQ -> Criteria.where(key).is(value);
                case NEQ -> Criteria.where(key).is(value).not();
                case GT -> Criteria.where(key).greaterThan(value);
                case GTE -> Criteria.where(key).greaterThanEqual(value);
                case LT -> Criteria.where(key).lessThan(value);
                case LTE -> Criteria.where(key).lessThanEqual(value);
                case LIKE -> Criteria.where(key).contains(String.valueOf(condition.getValue()));
                case NOT_LIKE -> Criteria.where(key).contains(String.valueOf(condition.getValue())).not();
                case RANGE -> {
                    List<?> range = (List<?>) value;
                    yield Criteria.where(key).between(range.get(0), range.get(1));
                }
                case IN -> Criteria.where(key).in(((Collection<?>) value).toArray());
                case NOT_IN -> Criteria.where(key).notIn(((Collection<?>) value).toArray());
                case NULL -> Criteria.where(key).exists().not();
                case NOT_NULL -> Criteria.where(key).exists();
                default -> null;
            };
            if (null != sub) criteria = null == criteria ? sub : criteria.and(sub);
        }
        return null == criteria ? new Criteria() : criteria;
    }

    private org.springframework.data.domain.Sort buildSort(EruptModel eruptModel, Page page) {
        List<Sort> sorts = null == page.getSort() || page.getSort().isEmpty()
                ? Sort.toSortList(eruptModel.getErupt().orderBy()) : page.getSort();
        org.springframework.data.domain.Sort springSort = org.springframework.data.domain.Sort.unsorted();
        for (Sort sort : sorts) {
            springSort = springSort.and(org.springframework.data.domain.Sort.by(
                    sort.getDirection() == Direction.DESC
                            ? org.springframework.data.domain.Sort.Direction.DESC
                            : org.springframework.data.domain.Sort.Direction.ASC,
                    sort.getField()));
        }
        return springSort;
    }

}
