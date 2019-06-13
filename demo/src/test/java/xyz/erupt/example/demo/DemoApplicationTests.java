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
        List list = entityManager.createQuery("from Test where testExtra.isMD5 is null").getResultList();
        System.out.println(list);
//        entityManager.createEntityGraph(EruptUser.class).
    }


}

