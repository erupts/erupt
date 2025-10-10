package xyz.erupt.jpa.dao;

import jakarta.annotation.Resource;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.jpa.service.EntityManagerService;

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

    @Getter
    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private EntityManagerService entityManagerService;

    @Getter
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Getter
    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SELECT = "select ";

    private static final String FROM = " from ";

    private static final String NEW_MAP = "new map(";

    private static final String AS = " as ";

    private static final String WHERE = " where ";

    @Deprecated
    public <T> T findById(Class<T> clazz, Object id) {
        this.entityManager.clear();
        return entityManager.find(clazz, id);
    }

    public <T> T find(Class<T> clazz, Object id) {
        return entityManager.find(clazz, id);
    }

    public void detach(Object obj) {
        this.entityManager.detach(obj);
    }

    //新增
    public void persist(Object obj) {
        entityManager.persist(obj);
    }

    @Transactional
    public void persistAndFlush(Object obj) {
        this.persist(obj);
        this.flush();
    }

    //修改
    public <T> T merge(T t) {
        return entityManager.merge(t);
    }

    @Transactional
    public <T> T mergeAndFlush(T t) {
        try {
            return this.merge(t);
        } finally {
            this.flush();
        }
    }

    //删除
    public void delete(Object obj) {
        if (entityManager.contains(obj)) {
            entityManager.remove(obj);
        } else {
            entityManager.remove(entityManager.merge(obj));
        }
    }

    @Transactional
    public void deleteAndFlush(Object obj) {
        try {
            this.delete(obj);
        } finally {
            this.flush();
        }
    }

    public void flush() {
        entityManager.flush();
    }

    @Comment("根据数据源名称获取 EntityManager 注意：必须手动执行 entityManager.close() 方法")
    public EntityManager getEntityManager(String name) {
        return entityManagerService.findEntityManager(name);
    }

    public <T> EruptLambdaQuery<T> lambdaQuery(Class<T> eruptClass) {
        return new EruptLambdaQuery<>(entityManager, eruptClass);
    }

    public <T> EruptLambdaQuery<T> lambdaQuery(EntityManager entityManager, Class<T> eruptClass) {
        return new EruptLambdaQuery<>(entityManager, eruptClass);
    }

    //不存在则新增
    public <T> T persistIfNotExist(Class<T> eruptClass, T obj, String field, String val) throws NonUniqueResultException {
        T t = (T) this.lambdaQuery(eruptClass).addCondition(field + " = :val", new HashMap<String, Object>(1) {{
            this.put("val", val);
        }}).one();
        if (null == t) {
            entityManager.persist(obj);
            entityManager.flush();
            return obj;
        }
        return t;
    }

}
