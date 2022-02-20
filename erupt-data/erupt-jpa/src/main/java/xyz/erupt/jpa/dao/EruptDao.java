package xyz.erupt.jpa.dao;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.jpa.service.EntityManagerService;

import javax.annotation.Resource;
import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2019-12-23
 */
@Component
public class EruptDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private EntityManagerService entityManagerService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SELECT = "select ";

    private static final String FROM = " from ";

    private static final String NEW_MAP = "new map(";

    private static final String AS = " as ";

    private static final String EQU = " = ";

    private static final String WHERE = " where ";

    //修改
    public <T> T merge(T t) {
        return entityManager.merge(t);
    }

    public <T> T mergeAndFlush(T t) {
        try {
            return this.merge(t);
        } finally {
            this.flush();
        }
    }

    public void flush() {
        entityManager.flush();
    }

    //删除
    public void delete(Object obj) {
        entityManager.remove(obj);
    }

    //新增
    public void persist(Object obj) {
        entityManager.persist(obj);
    }

    public void persistAndFlush(Object obj) {
        this.persist(obj);
        this.flush();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    @Comment("根据数据源名称获取 EntityManager 注意：必须手动执行 entityManager.close() 方法")
    public EntityManager getEntityManager(String name) {
        return entityManagerService.findEntityManager(name);
    }

    //不存在则新增
    public <T> T persistIfNotExist(Class<T> eruptClass, T obj, String field, String val) throws NonUniqueResultException {
        T t = (T) queryEntity(eruptClass, field + EQU + " :val", new HashMap<String, Object>(1) {{
            this.put("val", val);
        }});
        if (null == t) {
            entityManager.persist(obj);
            entityManager.flush();
            return obj;
        }
        return t;
    }

    //以下方法调用时需考虑sql注入问题，切勿随意传递expr参数值!!!
    public List<Map<String, Object>> queryMapList(Class<?> eruptClass, String expr, Map<String, Object> param, String... cols) {
        return simpleQuery(eruptClass, true, expr, param, cols).getResultList();
    }

    public List<Object[]> queryObjectList(Class<?> eruptClass, String expr, Map<String, Object> param, String... cols) {
        return simpleQuery(eruptClass, false, expr, param, cols).getResultList();
    }

    public <T> List<T> queryEntityList(Class<T> eruptClass, String expr, Map<String, Object> param) {
        return simpleQuery(eruptClass, false, expr, param).getResultList();
    }

    public <T> List<T> queryEntityList(Class<T> eruptClass, String expr) {
        return this.queryEntityList(eruptClass, expr, null);
    }

    public <T> List<T> queryEntityList(Class<T> eruptClass) {
        return this.queryEntityList(eruptClass, null);
    }

    public Map<String, Object> queryMap(Class<?> eruptClass, String expr, Map<String, Object> param, String... cols) throws NonUniqueResultException {
        try {
            return (Map<String, Object>) simpleQuery(eruptClass, true, expr, param, cols).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Object[] queryObject(Class<?> eruptClass, String expr, Map<String, Object> param, String... cols) throws NonUniqueResultException {
        try {
            return (Object[]) simpleQuery(eruptClass, false, expr, param, cols).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public <T> T queryEntity(Class<T> eruptClass, String expr, Map<String, Object> param) throws NonUniqueResultException {
        try {
            return (T) simpleQuery(eruptClass, false, expr, param).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public <T> T queryEntity(Class<T> eruptClass, String expr) {
        return this.queryEntity(eruptClass, expr, null);
    }

    public <T> T queryEntity(Class<T> eruptClass) {
        return this.queryEntity(eruptClass, null);
    }

    private Query simpleQuery(Class<?> eruptClass, boolean isMap, String expr, Map<String, Object> paramMap, String... cols) {
        StringBuilder sb = new StringBuilder();
        if (cols.length > 0) {
            sb.append(SELECT);
            if (isMap) {
                sb.append(NEW_MAP);
                for (int i = 0; i < cols.length; i++) {
                    sb.append(cols[i]).append(AS).append(cols[i]).append(i == cols.length - 1 ? "" : ",");
                }
                sb.append(")");
            } else {
                for (int i = 0; i < cols.length; i++) {
                    sb.append(cols[i]).append(i == cols.length - 1 ? "" : ",");
                }
            }
        }
        expr = StringUtils.isBlank(expr) ? "" : WHERE + expr;
        Query query = entityManager.createQuery(sb + FROM + eruptClass.getSimpleName() + expr);
        Optional.ofNullable(paramMap).ifPresent(map -> map.forEach(query::setParameter));
        return query;
    }
}
