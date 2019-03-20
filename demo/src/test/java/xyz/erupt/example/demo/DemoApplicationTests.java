package xyz.erupt.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.erupt.eruptlimit.model.EruptRole;
import xyz.erupt.eruptlimit.model.EruptUser;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void contextLoads() {
//        List list = entityManager.createQuery("from EruptUser where id=7").getResultList();
//        for (Object li : list) {
//            EruptUser eruptUser = (EruptUser) li;
//            Set<EruptRole> eruptRoles = eruptUser.getRoles();
//            System.out.println(eruptRoles);
//        }
    }

}

