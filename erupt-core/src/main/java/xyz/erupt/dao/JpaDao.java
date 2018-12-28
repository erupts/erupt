package xyz.erupt.dao;

import xyz.erupt.base.model.Page;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;

/**
 * Created by liyuepeng on 10/11/18.
 */
@Repository
public class JpaDao {

    @PersistenceContext
    private EntityManager entityManager;

    public void saveEntity(Object entity) {
        entityManager.persist(entity);
    }

    private void motifyData(Object ob) {

    }

    public void deleteEntity(Object entity) {
        entityManager.remove(entity);
    }

    public <T> T findDataById(Class<T> entity, Serializable id) {
        return entityManager.find(entity, id);
    }

    public Object queryByPage(Class<?> entity) {
        return entityManager.createQuery("from " + entity.getSimpleName());
    }

    ;

    public Page queryByPage(Class<?> entity, Page page) {
        page.setList(entityManager.createQuery("from " + entity.getSimpleName())
                .setMaxResults(page.getPageSize())
                .setFirstResult((page.getPageIndex() - 1) * page.getPageSize())
                .getResultList());
        return page;
    }


}
