package xyz.erupt.jpa.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import xyz.erupt.core.util.ReflectUtil;
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

    public <E, R> EruptLambdaQuery<T> with(SFunction<E, R> field) {
        querySchema.getWith().add(LambdaSee.field(field));
        return this;
    }

    public EruptLambdaQuery<T> with() {
        querySchema.getWith().clear();
        return this;
    }

    public <E, R> EruptLambdaQuery<T> isNull(SFunction<E, R> field) {
        querySchema.getWheres().add(geneField(field) + " is null");
        return this;
    }

    public <E, R> EruptLambdaQuery<T> isNull(boolean condition, SFunction<E, R> field) {
        if (condition) return this.isNull(field);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> isNotNull(SFunction<E, R> field) {
        querySchema.getWheres().add(geneField(field) + " is not null");
        return this;
    }

    public <E, R> EruptLambdaQuery<T> isNotNull(boolean condition, SFunction<E, R> field) {
        if (condition) return this.isNotNull(field);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> eq(SFunction<E, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " = :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> eq(boolean condition, SFunction<E, R> field, Object val) {
        if (condition) return this.eq(field, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> ne(SFunction<E, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " <> :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> ne(boolean condition, SFunction<E, R> field, Object val) {
        if (condition) return this.ne(field, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> gt(SFunction<E, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " > :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> gt(boolean condition, SFunction<E, R> field, Object val) {
        if (condition) return this.gt(field, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> lt(SFunction<E, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " < :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> lt(boolean condition, SFunction<E, R> field, Object val) {
        if (condition) return this.lt(field, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> ge(SFunction<E, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " >= :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> ge(boolean condition, SFunction<E, R> field, Object val) {
        if (condition) return this.ge(field, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> le(SFunction<E, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " <= :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> le(boolean condition, SFunction<E, R> field, Object val) {
        if (condition) return this.le(field, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> between(SFunction<E, R> field, Object val1, Object val2) {
        String l = this.genePlaceholder();
        String r = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " between :" + l + " and " + ":" + r);
        querySchema.getParams().put(l, val1);
        querySchema.getParams().put(r, val2);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> between(boolean condition, SFunction<E, R> field, Object val1, Object val2) {
        if (condition) return this.between(field, val1, val2);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> notBetween(SFunction<E, R> field, Object val1, Object val2) {
        String l = this.genePlaceholder();
        String r = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " not between :" + l + " and " + ":" + r);
        querySchema.getParams().put(l, val1);
        querySchema.getParams().put(r, val2);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> notBetween(boolean condition, SFunction<E, R> field, Object val1, Object val2) {
        if (condition) return this.notBetween(field, val1, val2);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> in(SFunction<E, R> field, Collection<?> val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " in (:" + placeholder + ")");
        querySchema.getParams().put(placeholder, new ArrayList<>(val));
        return this;
    }

    public <E, R> EruptLambdaQuery<T> in(boolean condition, SFunction<E, R> field, Collection<?> val) {
        if (condition) return this.in(field, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> in(SFunction<E, R> field, Object... val) {
        return this.in(field, Arrays.stream(val).collect(Collectors.toList()));
    }

    public <E, R> EruptLambdaQuery<T> in(boolean condition, SFunction<E, R> field, Object... val) {
        if (condition) return this.in(field, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> notIn(SFunction<E, R> field, Collection<?> val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " not in (:" + placeholder + ")");
        querySchema.getParams().put(placeholder, new ArrayList<>(val));
        return this;
    }

    public <E, R> EruptLambdaQuery<T> notIn(boolean condition, SFunction<E, R> field, Collection<?> val) {
        if (condition) return this.notIn(field, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> notIn(SFunction<E, R> field, Object... val) {
        return this.notIn(field, Arrays.stream(val).collect(Collectors.toList()));
    }

    public <E, R> EruptLambdaQuery<T> notIn(boolean condition, SFunction<E, R> field, Object... val) {
        if (condition) return this.notIn(field, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> like(SFunction<E, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " like :" + placeholder);
        querySchema.getParams().put(placeholder, "%" + val + "%");
        return this;
    }

    public <E, R> EruptLambdaQuery<T> like(boolean condition, SFunction<E, R> field, Object val) {
        if (condition) return this.like(field, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> likeValue(SFunction<E, R> field, Object val) {
        String placeholder = this.genePlaceholder();
        querySchema.getWheres().add(geneField(field) + " like :" + placeholder);
        querySchema.getParams().put(placeholder, val);
        return this;
    }

    public <E, R> EruptLambdaQuery<T> likeValue(boolean condition, SFunction<E, R> field, Object val) {
        if (condition) return this.likeValue(field, val);
        return this;
    }

    //添加自定义条件
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

    public <E> EruptLambdaQuery<T> orderBy(SFunction<E, ?> field) {
        querySchema.getOrders().add(geneField(field) + " asc");
        return this;
    }

    public <E> EruptLambdaQuery<T> orderBy(boolean condition, SFunction<E, ?> field) {
        if (condition) return this.orderBy(field);
        return this;
    }

    public <E> EruptLambdaQuery<T> orderByAsc(SFunction<E, ?> field) {
        return orderBy(field);
    }

    public <E> EruptLambdaQuery<T> orderByAsc(boolean condition, SFunction<E, ?> field) {
        return orderBy(condition, field);
    }


    public <E> EruptLambdaQuery<T> orderByDesc(SFunction<E, ?> field) {
        querySchema.getOrders().add(geneField(field) + " desc");
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

    public EruptLambdaQuery<T> distinct() {
        querySchema.setDistinct(true);
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
            Field f = ReflectUtil.findClassField(clazz, LambdaSee.field(fields[i]));
            f.setAccessible(true);
            f.set(r, objects[i]);
        }
        return r;
    }

    public Long count() {
        this.querySchema.columns.add("count(*)");
        return (Long) geneQuery().getSingleResult();
    }

    public <E> Long count(SFunction<E, ?> field) {
        this.querySchema.columns.add("count(" + geneField(field) + ")");
        return (Long) geneQuery().getSingleResult();
    }

    public <E> Object sum(SFunction<E, ?> field) {
        this.querySchema.columns.add("sum(" + geneField(field) + ")");
        return geneQuery().getSingleResult();
    }

    public <E> Double avg(SFunction<E, ?> field) {
        this.querySchema.columns.add("avg(" + geneField(field) + ")");
        return (Double) geneQuery().getSingleResult();
    }

    public <E> Object min(SFunction<E, ?> field) {
        this.querySchema.columns.add("min(" + geneField(field) + ")");
        return geneQuery().getSingleResult();
    }

    public <E> Object max(SFunction<E, ?> field) {
        this.querySchema.columns.add("max(" + geneField(field) + ")");
        return geneQuery().getSingleResult();
    }

    private Query geneQuery() {
        StringBuilder select = new StringBuilder();
        if (!querySchema.columns.isEmpty()) {
            select.append(SqlLang.SELECT);
            if (querySchema.distinct) {
                select.append(SqlLang.DISTINCT);
            }
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
        if (querySchema.with.isEmpty()) {
            return lambdaInfo.getClazz().getSimpleName() + SqlLang.DOT + lambdaInfo.getField();
        } else {
            StringBuilder withs = new StringBuilder();
            querySchema.with.forEach(it -> withs.append(it).append(SqlLang.DOT));
            return withs + lambdaInfo.getField();
        }
    }


    @Getter
    @Setter
    public static class QuerySchema {

        private List<String> with = new ArrayList<>();

        private List<String> columns = new ArrayList<>();

        private Map<String, Object> params = new HashMap<>();

        private List<String> wheres = new ArrayList<>();

        private List<String> orders = new ArrayList<>();

        private Integer limit;

        private Integer offset;

        private boolean distinct = false;

    }

}
