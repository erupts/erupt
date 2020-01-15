package xyz.erupt.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.core.config.EruptConfig;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.service.data_impl.DBService;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.service.EruptJobService;
import xyz.erupt.tool.EruptDao;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DBService dbService;

    public static final String DATASOURCE_PREFIX = "spring.datasource.";

    @Autowired
    private EruptDao eruptDao;

    @Test
    public void testJob() throws ParseException, SchedulerException {
        EruptJob eruptJob = new EruptJob();
        eruptJobService.modifyJob(eruptJob);
    }

    @Test
    public void eruptDaoObj() {
        Object[] oo = eruptDao.queryObject(EruptUser.class, "account = 'erupt'", "id", "name");
        System.out.println(oo[0] + ":" + oo[1]);
    }

    @Test
    public void eruptDaoMap() {
        try {
            Map<String, Object> map = eruptDao.queryMap(EruptUser.class,
                    "account = '2222'", "id", "name");
            for (String s : map.keySet()) {
                System.out.println(s + ":" + map.get(s));
            }
        } catch (NoResultException e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void eruptDao() {
        List<EruptUser> list = eruptDao.queryEntityList(EruptUser.class, "1=1 order by account desc");
        for (EruptUser user : list) {
            System.out.println(user.getAccount());
        }
    }

    @Test
    public void erupt() {

        int i = 100000;
        for (int i1 = 0; i1 < i; i1++) {
            CoreService.getErupt("EruptUser");
        }

    }

    @Autowired
    private EruptJobService eruptJobService;
    @Resource
    private DataSourceProperties dataSourceProperties;
    @Resource
    private Environment env;

    @Autowired
    private EruptConfig eruptConfig;

    @Test
    public void getProperties() {
        System.out.println(dataSourceProperties.getUrl());
        System.out.println(env.getProperty("server.compression.mime-types"));
        System.out.println(env.getProperty("spring.resources.datasource.url"));
        System.out.println(env.getProperty("erupt.upload-path"));
    }

    @Test
    public void craeteEntityManager() {
        String sourceName = "one";
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        {
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            vendorAdapter.setGenerateDdl(false);
            vendorAdapter.setDatabase(Database.SQL_SERVER);
            vendorAdapter.setShowSql(true);
            factory.setJpaVendorAdapter(vendorAdapter);
        }
        {
            factory.setDataSource(DataSourceBuilder.create()
                    .url(env.getProperty(DATASOURCE_PREFIX + sourceName + ".url"))
                    .username(env.getProperty(DATASOURCE_PREFIX + sourceName + ".username"))
                    .password(env.getProperty(DATASOURCE_PREFIX + sourceName + ".password"))
                    .build());
        }
        factory.setPackagesToScan(eruptConfig.getScannerPackage());
        factory.afterPropertiesSet();
        EntityManager entityManager = factory.getObject().createEntityManager();
        List list = entityManager.createNativeQuery("select * from t_xinwen").getResultList();
        for (Object o : list) {
            System.out.println(o);
        }
    }
}

