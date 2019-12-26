package xyz.erupt.tool.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2019-12-23
 */
@Component
public class EruptDao {

    @PersistenceContext
    private EntityManager entityManager;

    public static final String SELECT = "select ";

    private static final String FROM = " from ";
    public static final String NEW_MAP = "new map(";

    private static final String AND = " and ";

    private static final String AS = " as ";
    public static final String EQU = " = ";
    private static final String WHERE = " where ";

    public void persistIfNotExist(Object obj, String field, String val) {
        Object[] o = queryObject(obj.getClass(), field + EQU + "'" + val + "'", field);
        if (null == o) {
            entityManager.persist(obj);
        }
    }

    private Query simpleQuery(Class eruptClass, boolean isMap, String expr, String... cols) {
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
        return entityManager.createQuery(sb.toString() + FROM + eruptClass.getSimpleName() + expr);
    }

    //以下方法调用时需考虑sql注入问题，切勿随意传递expr参数值!!!
    public List<Map<String, Object>> queryMapList(Class eruptClass, String expr, String... cols) {
        return simpleQuery(eruptClass, true, expr, cols).getResultList();
    }

    public List<Object[]> queryObjectList(Class eruptClass, String expr, String... cols) {
        return simpleQuery(eruptClass, false, expr, cols).getResultList();
    }

    public <T> List<T> queryEntityList(Class<T> eruptClass, String expr) {
        return simpleQuery(eruptClass, false, expr).getResultList();
    }

    public Map<String, Object> queryMap(Class eruptClass, String expr, String... cols) throws NoResultException, NonUniqueResultException {
        return (Map<String, Object>) simpleQuery(eruptClass, true, expr, cols).getSingleResult();
    }

    public Object[] queryObject(Class eruptClass, String expr, String... cols) throws NoResultException, NonUniqueResultException {
        return (Object[]) simpleQuery(eruptClass, false, expr, cols).getSingleResult();
    }

    public <T> T queryEntity(Class<T> eruptClass, String expr) throws NoResultException, NonUniqueResultException {
        return (T) simpleQuery(eruptClass, false, expr).getSingleResult();
    }

}
