package xyz.erupt.jpa.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.core.annotation.Order;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.core.annotation.EruptDataSource;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.prop.EruptPropDb;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.util.*;
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
    public void run(ApplicationArguments args) {
        if (null != eruptProp.getDbs()) {
            //多数据源处理
            entityManagerMap = new HashMap<>();
            for (EruptPropDb prop : eruptProp.getDbs()) {
                Objects.requireNonNull(prop.getDatasource().getName(), "dbs configuration Must specify name → dbs.datasource.name");
                Objects.requireNonNull(prop.getScanPackages(), String.format("%s DataSource not found 'scanPackages' configuration", prop.getDatasource().getName()));
                LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
                {
                    JpaProperties jpa = prop.getJpa();
                    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
                    vendorAdapter.setGenerateDdl(jpa.isGenerateDdl());
                    vendorAdapter.setDatabase(jpa.getDatabase());
                    vendorAdapter.setShowSql(jpa.isShowSql());
                    vendorAdapter.setDatabasePlatform(jpa.getDatabasePlatform());
                    factory.setJpaVendorAdapter(vendorAdapter);
                    Properties properties = new Properties();
                    properties.putAll(jpa.getProperties());
                    factory.setJpaProperties(properties);
                }
                {
                    HikariConfig hikariConfig = prop.getDatasource().getHikari().toHikariConfig();
                    Optional.ofNullable(prop.getDatasource().getUrl()).ifPresent(hikariConfig::setJdbcUrl);
                    Optional.ofNullable(prop.getDatasource().getDriverClassName()).ifPresent(hikariConfig::setDriverClassName);
                    Optional.ofNullable(prop.getDatasource().getUsername()).ifPresent(hikariConfig::setUsername);
                    Optional.ofNullable(prop.getDatasource().getPassword()).ifPresent(hikariConfig::setPassword);
                    Optional.ofNullable(prop.getDatasource().getHikari().getPoolName()).ifPresent(hikariConfig::setPoolName);
                    factory.setDataSource(new HikariDataSource(hikariConfig));
                    factory.setPackagesToScan(prop.getScanPackages());
                    factory.afterPropertiesSet();
                }
                entityManagerMap.put(prop.getDatasource().getName(), factory.getObject());
            }
        }
    }


    public <R> R getEntityManager(Class<?> eruptClass, Function<EntityManager, R> function) {
        EruptDataSource eruptDataSource = eruptClass.getAnnotation(EruptDataSource.class);
        if (null == eruptDataSource) return function.apply(entityManager);
        EntityManager em = entityManagerMap.get(eruptDataSource.value()).createEntityManager();
        try {
            return function.apply(em);
        } finally {
            if (em.isOpen()) em.close();
        }
    }


    public void entityManagerTran(Class<?> eruptClass, Consumer<EntityManager> consumer) {
        EruptDataSource eruptDataSource = eruptClass.getAnnotation(EruptDataSource.class);
        if (null == eruptDataSource) {
            consumer.accept(entityManager);
            return;
        }
        EntityManager em = entityManagerMap.get(eruptDataSource.value()).createEntityManager();
        try {
            em.getTransaction().begin();
            consumer.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Comment("必须手动执行 close() 方法")
    public EntityManager findEntityManager(String name) {
        return entityManagerMap.get(name).createEntityManager();
    }
}
