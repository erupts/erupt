package xyz.erupt.tool;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2019-12-23
 */
@Component
public class EruptDbFun {

    @PersistenceContext
    private EntityManager entityManager;

    public void persistIfNotExist(Object obj, String field, String val) {
        List list = entityManager
                .createQuery("from " + obj.getClass()
                        .getSimpleName() + " where " + field + " = '" + val + "'").getResultList();
        if (list.isEmpty()) {
            entityManager.persist(obj);
        }
    }
}
