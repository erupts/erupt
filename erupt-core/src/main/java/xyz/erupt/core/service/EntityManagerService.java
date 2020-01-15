package xyz.erupt.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Service;
import xyz.erupt.core.annotation.EruptDataSource;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.config.EruptConfig;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author liyuepeng
 * @date 2020-01-13
 */
@Service
public class EntityManagerService {

    private static final String DATASOURCE_PREFIX = "spring.datasource.";
    @PersistenceContext
    private EntityManager entityManager;
    @Resource
    private Environment env;
    @Autowired
    private EruptConfig eruptConfig;

    public EntityManager getEntityManager(EruptModel eruptModel) {
        EruptDataSource eruptDataSource = eruptModel.getClazz().getAnnotation(EruptDataSource.class);
        if (null != eruptDataSource) {
            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            {
                HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
                vendorAdapter.setGenerateDdl(false);
                vendorAdapter.setDatabase(eruptDataSource.database());
                vendorAdapter.setShowSql(true);
                factory.setJpaVendorAdapter(vendorAdapter);
            }
            factory.setDataSource(DataSourceBuilder.create()
                    .url(env.getProperty(DATASOURCE_PREFIX + eruptDataSource.sourceName() + ".url"))
                    .username(env.getProperty(DATASOURCE_PREFIX + eruptDataSource.sourceName() + ".username"))
                    .password(env.getProperty(DATASOURCE_PREFIX + eruptDataSource.sourceName() + ".password"))
                    .build());
            factory.setPackagesToScan(eruptConfig.getScannerPackage());
            factory.afterPropertiesSet();
            return factory.getObject().createEntityManager();
        } else {
            return entityManager;
        }
    }

}
