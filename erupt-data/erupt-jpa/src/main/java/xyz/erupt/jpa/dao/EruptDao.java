package xyz.erupt.jpa.dao;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2019-12-23
 */
@Component
public class EruptDao {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String SELECT = "select ";

    private static final String FROM = " from ";

    private static final String NEW_MAP = "new map(";

    private static final String AND = " and ";

    private static final String AS = " as ";

    private static final String EQU = " = ";

    private static final String WHERE = " where ";

    //修改
    public <T> T merge(T t) {
        return entityManager.merge(t);
    }

    //删除
    public void delete(Object obj) {
        entityManager.remove(obj);
    }

    //新增
    public void persist(Object obj) {
        entityManager.persist(obj);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    //不存在则新增
    public <T> T persistIfNotExist(Class<T> eruptClass, Object obj, String field, String val) throws NonUniqueResultException {
        T t = (T) queryEntity(obj.getClass(), field + EQU + " :val", new HashMap<String, Object>(1) {
            {
                this.put("val", val);
            }
        });
        if (null == t) {
            entityManager.persist(obj);
            entityManager.flush();
            return (T) obj;
        } else {
            return t;
        }
    }

    private Query simpleQuery(Class eruptClass, boolean isMap, String expr, Map<String, Object> paramMap, String... cols) {
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
        Query query = entityManager.createQuery(sb.toString() + FROM + eruptClass.getSimpleName() + expr);
        if (null != paramMap) {
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
        }
        return query;
    }

    //以下方法调用时需考虑sql注入问题，切勿随意传递expr参数值!!!
    public List<Map<String, Object>> queryMapList(Class eruptClass, String expr, Map<String, Object> param, String... cols) {
        return simpleQuery(eruptClass, true, expr, param, cols).getResultList();
    }

    public List<Object[]> queryObjectList(Class eruptClass, String expr, Map<String, Object> param, String... cols) {
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

    public Map<String, Object> queryMap(Class eruptClass, String expr, Map<String, Object> param, String... cols) throws NonUniqueResultException {
        try {
            return (Map<String, Object>) simpleQuery(eruptClass, true, expr, param, cols).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Object[] queryObject(Class eruptClass, String expr, Map<String, Object> param, String... cols) throws NonUniqueResultException {
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
}
