package xyz.erupt.jpa.query;

import org.apache.commons.lang3.RandomStringUtils;
import xyz.erupt.core.lambda.LambdaReflect;
import xyz.erupt.core.lambda.SFunction;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2024/3/30 23:23
 */
public class EruptLambdaQuery<T> {

    private final QuerySchema querySchema = new QuerySchema();

    private final EntityManager entityManager;

    private final Class<T> eruptClass;

    public EruptLambdaQuery(EntityManager entityManager, Class<T> eruptClass) {
        this.entityManager = entityManager;
        this.eruptClass = eruptClass;
    }

    public <R> EruptLambdaQuery<T> isNull(SFunction<T, R> field) {
        querySchema.getWheres().add(LambdaReflect.info(field).getField() + " is null");
        return this;
    }

    public <R> EruptLambdaQuery<T> notNotNull(SFunction<T, R> field) {
        querySchema.getWheres().add(LambdaReflect.info(field).getField() + " is not null");
        return this;
    }

    public <R> EruptLambdaQuery<T> eq(SFunction<T, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(LambdaReflect.info(field).getField() + " = :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> ne(SFunction<T, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(LambdaReflect.info(field).getField() + " != :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> between(SFunction<T, R> field, Object val1, Object val2) {
        String l = this.genePlaceholder();
        String r = this.genePlaceholder();
        querySchema.getWheres().add(LambdaReflect.info(field).getField() + " between :" + l + " and " + ":" + r);
        querySchema.getParams().put(l, val1);
        querySchema.getParams().put(r, val2);
        return this;
    }

    public <R> EruptLambdaQuery<T> in(SFunction<T, R> field, List<Object> val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(LambdaReflect.info(field).getField() + " in (:" + placeholder + ")");
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> in(SFunction<T, R> field, Object... val) {
        return this.in(field, Arrays.stream(val).collect(Collectors.toList()));
    }

    public EruptLambdaQuery<T> orderAsc(SFunction<T, ?> field) {
        querySchema.getOrders().add(LambdaReflect.info(field).getField() + " asc");
        return this;
    }

    public EruptLambdaQuery<T> orderDesc(SFunction<T, ?> field) {
        querySchema.getOrders().add(LambdaReflect.info(field).getField() + " asc");
        return this;
    }

    public EruptLambdaQuery<T> limit(Integer limit) {
        querySchema.setLimit(limit);
        return this;
    }

    public EruptLambdaQuery<T> offset(Integer offset) {
        querySchema.setOffset(offset);
        return this;
    }

    public T one() {
        return (T) geneQuery().getSingleResult();
    }

    public List<T> list() {
        return geneQuery().getResultList();
    }

    private Query geneQuery() {
        StringBuilder expr = new StringBuilder("from " + eruptClass.getSimpleName());
        if (!querySchema.getWheres().isEmpty()) {
            expr.append(" where ").append(String.join(" and ", querySchema.getWheres()));
        }
        if (!querySchema.getOrders().isEmpty()) {
            expr.append(" order by ").append(String.join(",", querySchema.getOrders()));
        }
        Query query = entityManager.createQuery(expr.toString());
        if (null != querySchema.getLimit()) {
            query.setMaxResults(querySchema.getLimit());
        }
        if (null != querySchema.getOffset()) {
            query.setFirstResult(querySchema.getOffset());
        }
        return query;
    }

    private String genePlaceholder() {
        return RandomStringUtils.randomAlphabetic(4);
    }


}
