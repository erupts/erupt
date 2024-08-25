package xyz.erupt.jpa.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import xyz.erupt.jpa.constant.SqlLang;
import xyz.erupt.linq.lambda.LambdaInfo;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.linq.lambda.SFunction;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.util.*;
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
        querySchema.getWheres().add(LambdaSee.field(field) + " is null");
        return this;
    }

    public <R> EruptLambdaQuery<T> isNull(boolean condition, SFunction<T, R> field) {
        if (condition) return this.isNull(field);
        return this;
    }

    public <R> EruptLambdaQuery<T> isNotNull(SFunction<T, R> field) {
        querySchema.getWheres().add(LambdaSee.field(field) + " is not null");
        return this;
    }

    public <R> EruptLambdaQuery<T> isNotNull(boolean condition, SFunction<T, R> field) {
        if (condition) return this.isNotNull(field);
        return this;
    }

    public <R> EruptLambdaQuery<T> eq(SFunction<T, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " = :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> eq(boolean condition, SFunction<T, R> field, Object val) {
        if (condition) return this.eq(field, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> ne(SFunction<T, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(LambdaSee.field(field) + " <> :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> ne(boolean condition, SFunction<T, R> field, Object val) {
        if (condition) return this.ne(field, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> gt(SFunction<T, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " > :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> gt(boolean condition, SFunction<T, R> field, Object val) {
        if (condition) return this.gt(field, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> lt(SFunction<T, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " < :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> lt(boolean condition, SFunction<T, R> field, Object val) {
        if (condition) return this.lt(field, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> ge(SFunction<T, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " >= :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> ge(boolean condition, SFunction<T, R> field, Object val) {
        if (condition) return this.ge(field, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> le(SFunction<T, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " <= :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> le(boolean condition, SFunction<T, R> field, Object val) {
        if (condition) return this.le(field, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> between(SFunction<T, R> field, Object val1, Object val2) {
        String l = this.genePlaceholder();
        String r = this.genePlaceholder();
        querySchema.getWheres().add(LambdaSee.field(field) + " between :" + l + " and " + ":" + r);
        querySchema.getParams().put(l, val1);
        querySchema.getParams().put(r, val2);
        return this;
    }

    public <R> EruptLambdaQuery<T> between(boolean condition, SFunction<T, R> field, Object val1, Object val2) {
        if (condition) return this.between(field, val1, val2);
        return this;
    }

    public <R> EruptLambdaQuery<T> in(SFunction<T, R> field, Collection<?> val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(LambdaSee.field(field) + " in (:" + placeholder + ")");
        querySchema.getParams().put(placeholder, new ArrayList<>(val));
        return this;
    }

    public <R> EruptLambdaQuery<T> in(boolean condition, SFunction<T, R> field, Collection<?> val) {
        if (condition) return this.in(field, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> in(SFunction<T, R> field, Object... val) {
        return this.in(field, Arrays.stream(val).collect(Collectors.toList()));
    }

    public <R> EruptLambdaQuery<T> in(boolean condition, SFunction<T, R> field, Object... val) {
        if (condition) return this.in(field, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> like(SFunction<T, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " like :" + placeholder);
        querySchema.getParams().put(placeholder, "%" + val + "%");
        return this;
    }

    public <R> EruptLambdaQuery<T> like(boolean condition, SFunction<T, R> field, Object val) {
        if (condition) return this.like(field, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> likeValue(SFunction<T, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " like :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <R> EruptLambdaQuery<T> likeValue(boolean condition, SFunction<T, R> field, Object val) {
        if (condition) return this.likeValue(field, val);
        return this;
    }

    //添加条件
    public EruptLambdaQuery<T> addCondition(String condition) {
        querySchema.getWheres().add(condition);
        return this;
    }

    /**
     * 添加自定义条件
     *
     * @param condition :xxx 的占位符可以被 params参数运行时替换，防止 SQL 注入
     * @param params    条件参数
     */
    public EruptLambdaQuery<T> addCondition(String condition, Map<String, Object> params) {
        querySchema.getWheres().add(condition);
        Optional.ofNullable(params).ifPresent(it -> querySchema.getParams().putAll(params));
        return this;
    }

    public EruptLambdaQuery<T> addParam(String key, Object val) {
        querySchema.getParams().put(key, val);
        return this;
    }

    public EruptLambdaQuery<T> orderBy(SFunction<T, ?> field) {
        querySchema.getOrders().add(LambdaSee.field(field) + " asc");
        return this;
    }

    public EruptLambdaQuery<T> orderBy(boolean condition, SFunction<T, ?> field) {
        if (condition) return this.orderBy(field);
        return this;
    }

    public EruptLambdaQuery<T> orderByAsc(SFunction<T, ?> field) {
        return orderBy(field);
    }

    public EruptLambdaQuery<T> orderByAsc(boolean condition, SFunction<T, ?> field) {
        return orderBy(condition, field);
    }


    public EruptLambdaQuery<T> orderByDesc(SFunction<T, ?> field) {
        querySchema.getOrders().add(LambdaSee.field(field) + " desc");
        return this;
    }

    public EruptLambdaQuery<T> orderByDesc(boolean condition, SFunction<T, ?> field) {
        if (condition) return this.orderByDesc(field);
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
        try {
            return (T) this.geneQuery().getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<T> list() {
        return this.geneQuery().getResultList();
    }

    public final <S> S oneSelect(SFunction<T, S> field) {
        this.querySchema.columns.add(LambdaSee.field(field));
        try {
            return (S) this.geneQuery().getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public final <S> List<S> listSelect(SFunction<T, S> field) {
        this.querySchema.columns.add(LambdaSee.field(field));
        return this.geneQuery().getResultList();
    }

    @SafeVarargs
    public final <R> List<R> listSelect(Class<R> requiredType, SFunction<T, ?>... fields) {
        for (SFunction<T, ?> field : fields) this.querySchema.columns.add(LambdaSee.field(field));
        List<Object[]> objects = this.geneQuery().getResultList();
        return objects.stream().map(it -> objectToClazz(requiredType, it, fields)).collect(Collectors.toList());
    }

    @SafeVarargs
    public final <R> R oneSelect(Class<R> requiredType, SFunction<T, ?>... fields) {
        for (SFunction<T, ?> field : fields) this.querySchema.columns.add(LambdaSee.field(field));
        try {
            return objectToClazz(requiredType, (Object[]) this.geneQuery().getSingleResult(), fields);
        } catch (NoResultException e) {
            return null;
        }
    }

    @Deprecated
    @SafeVarargs
    public final List<Object[]> listSelects(SFunction<T, ?>... fields) {
        for (SFunction<T, ?> field : fields) this.querySchema.columns.add(LambdaSee.field(field));
        return this.geneQuery().getResultList();
    }

    @Deprecated
    @SafeVarargs
    public final Object[] oneSelects(SFunction<T, ?>... fields) {
        for (SFunction<T, ?> field : fields) this.querySchema.columns.add(LambdaSee.field(field));
        try {
            return (Object[]) this.geneQuery().getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @SneakyThrows
    private <R> R objectToClazz(Class<R> clazz, Object[] objects, SFunction<?, ?>... fields) {
        R r = clazz.newInstance();
        for (int i = 0; i < fields.length; i++) {
            Field f = clazz.getDeclaredField(LambdaSee.field(fields[i]));
            f.setAccessible(true);
            f.set(r, objects[i]);
        }
        return r;
    }

    public Long count() {
        this.querySchema.columns.add("count(*)");
        return (Long) geneQuery().getSingleResult();
    }

    public Long count(SFunction<T, ?> field) {
        this.querySchema.columns.add("count(" + LambdaSee.field(field) + ")");
        return (Long) geneQuery().getSingleResult();
    }

    public Object sum(SFunction<T, ?> field) {
        this.querySchema.columns.add("sum(" + LambdaSee.field(field) + ")");
        return geneQuery().getSingleResult();
    }

    public Double avg(SFunction<T, ?> field) {
        this.querySchema.columns.add("avg(" + LambdaSee.field(field) + ")");
        return (Double) geneQuery().getSingleResult();
    }

    public Object min(SFunction<T, ?> field) {
        this.querySchema.columns.add("min(" + LambdaSee.field(field) + ")");
        return geneQuery().getSingleResult();
    }

    public Object max(SFunction<T, ?> field) {
        this.querySchema.columns.add("max(" + LambdaSee.field(field) + ")");
        return geneQuery().getSingleResult();
    }

    private Query geneQuery() {
        StringBuilder select = new StringBuilder();
        if (!querySchema.columns.isEmpty()) {
            select.append(SqlLang.SELECT);
            querySchema.getColumns().forEach(it -> select.append(it).append(SqlLang.COMMA));
            select.deleteCharAt(select.length() - 1);
        }
        StringBuilder expr = new StringBuilder(select + SqlLang.FROM + eruptClass.getSimpleName() + SqlLang.AS + eruptClass.getSimpleName());
        if (!querySchema.getWheres().isEmpty())
            expr.append(SqlLang.WHERE).append(String.join(SqlLang.AND, querySchema.getWheres()));
        if (!querySchema.getOrders().isEmpty())
            expr.append(SqlLang.ORDER_BY).append(String.join(SqlLang.COMMA, querySchema.getOrders()));
        Query query = entityManager.createQuery(expr.toString());
        querySchema.getParams().forEach(query::setParameter);
        Optional.ofNullable(querySchema.getLimit()).ifPresent(query::setMaxResults);
        Optional.ofNullable(querySchema.getOffset()).ifPresent(query::setFirstResult);
        return query;
    }

    private String genePlaceholder() {
        return RandomStringUtils.randomAlphabetic(4);
    }

    private String geneField(SFunction<?, ?> field) {
        LambdaInfo lambdaInfo = LambdaSee.info(field);
        return lambdaInfo.getClazz().getSimpleName() + "." + lambdaInfo.getField();
    }

    @Getter
    @Setter
    public static class QuerySchema {

        private List<String> columns = new ArrayList<>();

        private Map<String, Object> params = new HashMap<>();

        private List<String> wheres = new ArrayList<>();

        private List<String> orders = new ArrayList<>();

        private Integer limit;

        private Integer offset;

    }

}
