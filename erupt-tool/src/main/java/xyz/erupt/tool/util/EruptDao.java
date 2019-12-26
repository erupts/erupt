package xyz.erupt.tool.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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

    private static final String FROM = " from ";

    private static final String AND = " and ";

    private static final String AS = " as ";

    public void persistIfNotExist(Object obj, String field, String val) {
        Object[] o = queryObj(obj.getClass(), field + " = '" + val + "'", "id");
        if (o == null) {
            entityManager.persist(obj);
        }
    }

    private Query simpleQuery(Class eruptClass, boolean isMap, String condition, String... cols) {
        StringBuilder sb = new StringBuilder();
        if (cols.length > 0) {
            sb.append("select ");
            if (isMap) {
                sb.append("new map(");
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
        condition = StringUtils.isBlank(condition) ? "" : AND + condition;
        return entityManager.createQuery(sb.toString() + FROM + eruptClass.getSimpleName() +
                " where 1=1 " + condition);
    }

    public List<Map<String, Object>> queryMapList(Class eruptClass, String condition, String... cols) {
        return simpleQuery(eruptClass, true, condition, cols).getResultList();
    }

    public List<Object[]> queryObjList(Class eruptClass, String condition, String... cols) {
        return simpleQuery(eruptClass, false, condition, cols).getResultList();
    }

    public <T> List<T> queryEntityList(Class<T> eruptClass, String condition) {
        return simpleQuery(eruptClass, false, condition).getResultList();
    }

    public Map<String, Object> queryMap(Class eruptClass, String condition, String... cols) {
        return (Map<String, Object>) simpleQuery(eruptClass, true, condition, cols).getSingleResult();
    }

    public Object[] queryObj(Class eruptClass, String condition, String... cols) {
        return (Object[]) simpleQuery(eruptClass, false, condition, cols).getSingleResult();
    }

    public <T> T queryEntity(Class<T> eruptClass, String condition) {
        return (T) simpleQuery(eruptClass, false, condition).getSingleResult();
    }

}
