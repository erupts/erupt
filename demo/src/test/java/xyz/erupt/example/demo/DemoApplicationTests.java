package xyz.erupt.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.core.service.data_impl.DBService;
import xyz.erupt.job.model.EruptJobModel;
import xyz.erupt.job.service.EruptJobService;
import xyz.erupt.tool.util.EruptDao;

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

    @Autowired
    EruptJobService eruptJobService;
    @Autowired
    private EruptDao eruptDao;

    @Test
    public void testJob() throws ParseException, SchedulerException {
        EruptJobModel eruptJobModel = new EruptJobModel();
        eruptJobModel.setCode("aaa");
        eruptJobModel.setCron("* * * * * ? *");
        eruptJobModel.setStatus(true);
        eruptJobService.modifyJob(eruptJobModel);
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

}

