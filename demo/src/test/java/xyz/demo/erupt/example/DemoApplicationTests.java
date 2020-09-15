package xyz.demo.erupt.example;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.auth.util.IpUtil;
import xyz.erupt.core.config.EruptProp;
import xyz.erupt.core.dao.EruptDao;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.data_impl.EruptDbService;
import xyz.erupt.core.test.EruptRunner;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.service.EruptJobService;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(EruptRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EruptDbService dbService;

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
        Object[] oo = eruptDao.queryObject(EruptUser.class, "account = 'erupt'", null, "id", "name");
        Assert.assertEquals(oo.length, 2);
        System.out.println(oo[0] + ":" + oo[1]);
    }

    @Test
    public void eruptDaoMap() {
        try {
            Map<String, Object> map = eruptDao.queryMap(EruptUser.class,
                    "account = '2222'", null, "id", "name");
            for (String s : map.keySet()) {
                System.out.println(s + ":" + map.get(s));
            }
        } catch (NoResultException e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void eruptDao() {
        List<EruptUser> list = eruptDao.queryEntityList(EruptUser.class, "1=1 order by account desc", null);
        for (EruptUser user : list) {
            System.out.println(user.getAccount());
        }
    }

    @Test
    public void erupt() {
        int i = 1000;
        long start = System.currentTimeMillis();
        for (int i1 = 0; i1 < i; i1++) {
            EruptCoreService.getErupt("Demo");
        }
        System.out.println(((System.currentTimeMillis() - start) / 1000) + 's');
    }

    @Autowired
    private EruptJobService eruptJobService;

    @Resource
    private DataSourceProperties dataSourceProperties;

    @Resource
    private Environment env;

    @Autowired
    private EruptProp eruptProp;

    @Test
    public void getProperties() {
        System.out.println(dataSourceProperties.getUrl());
        System.out.println(env.getProperty("server.compression.mime-types"));
        System.out.println(env.getProperty("spring.resources.datasource.url"));
        System.out.println(env.getProperty("erupt.upload-path"));
    }

    @Test
    public void joinTest() {
        entityManager.createQuery("select eruptUserTree.name from Demo where choice='xxx'").getResultList();
    }

    @Autowired
    private HttpServletRequest request;

    @Test
    public void ipCity() {
        System.out.println(IpUtil.getCityInfo(IpUtil.getIpAddr(request)));
    }

    @Test
    public void testScriptengine() throws ScriptException {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
        for (int i = 0; i < 1000; i++) {
            System.out.println(scriptEngine.eval("1+1"));
        }
    }


    @Test
    public void curr() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        for (int i = 0; i < 1000000; i++) {
            executorService.submit(() -> {
                EruptCoreService.getEruptView("Demo");
            });
        }
    }

}

