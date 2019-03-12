package xyz.erupt.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void contextLoads() {
        List list = entityManager.createQuery("select name from SubMmo").getResultList();
        System.out.println(list);
//        String hql = "select new map(EruptRole.id as id,EruptRole.name as name,EruptRole.remark as remark,EruptRole.status as status) from EruptRole EruptRole where 1=1";
//        List list = entityManager.createQuery(hql).getResultList();
//        System.out.println(list);
    }

}

