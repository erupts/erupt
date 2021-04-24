package xyz.erupt.jpa.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.core.annotation.Order;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.core.annotation.EruptDataSource;
import xyz.erupt.core.config.EruptProp;
import xyz.erupt.core.config.EruptPropDb;
import xyz.erupt.core.service.EruptApplication;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author YuePeng
 * date 2020-01-13
 */
@Service
@Order
public class EntityManagerService implements ApplicationRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private EruptProp eruptProp;

    private Map<String, EntityManagerFactory> entityManagerMap;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (null != eruptProp.getDbs()) {
            //多数据源处理
            entityManagerMap = new HashMap<>();
            for (EruptPropDb prop : eruptProp.getDbs()) {
                LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
                {
                    JpaProperties jpa = prop.getJpa();
                    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
                    vendorAdapter.setGenerateDdl(false);
                    vendorAdapter.setDatabase(jpa.getDatabase());
                    vendorAdapter.setShowSql(jpa.isShowSql());
                    vendorAdapter.setDatabase(jpa.getDatabase());
                    vendorAdapter.setDatabasePlatform(jpa.getDatabasePlatform());
                    factory.setJpaVendorAdapter(vendorAdapter);
                }
                {
                    factory.setDataSource(prop.getDatasource().initializeDataSourceBuilder().build());
                    factory.setPackagesToScan(EruptApplication.getScanPackage());
                    factory.afterPropertiesSet();
                }
                entityManagerMap.put(prop.getDatasource().getName(), factory.getObject());
            }
        }
    }


    public <R> R getEntityManager(Class<?> eruptClass, Function<EntityManager, R> function) {
        EruptDataSource eruptDataSource = eruptClass.getAnnotation(EruptDataSource.class);
        if (null == eruptDataSource) {
            return function.apply(entityManager);
        } else {
            EntityManager em = entityManagerMap.get(eruptDataSource.value()).createEntityManager();
            try {
                return function.apply(em);
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }
    }


    public void entityManagerTran(Class<?> eruptClass, Consumer<EntityManager> consumer) {
        EruptDataSource eruptDataSource = eruptClass.getAnnotation(EruptDataSource.class);
        if (null == eruptDataSource) {
            consumer.accept(entityManager);
        } else {
            EntityManager em = entityManagerMap.get(eruptDataSource.value()).createEntityManager();
            try {
                em.getTransaction().begin();
                consumer.accept(em);
                em.getTransaction().commit();
            } catch (Exception e) {
                e.printStackTrace();
                em.getTransaction().rollback();
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }
    }

    @Comment("必须手动执行 close() 方法")
    public EntityManager findEntityManager(String name) {
        return entityManagerMap.get(name).createEntityManager();
    }
}
