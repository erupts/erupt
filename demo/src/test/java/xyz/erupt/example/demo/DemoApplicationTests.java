package xyz.erupt.example.demo;

import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.erupt.eruptlimit.model.EruptRole;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void contextLoads() {

    }

    public static void main(String[] args) {
        String json = "{\"code\":\"demo2\",\"name\":\"demo2\",\"status\":true,\"id\":2," +
                "\"menus\":[{\"id\":\"16\"},{\"id\":\"6\"},{\"id\":\"9\"},{\"id\":\"8\"}]," +
                "\"users\":[{\"id\":\"8\"},{\"id\":\"9\"},{\"id\":\"22\"},{\"id\":\"24\"},{\"id\":\"62\"},{\"id\":\"26\"}]}";
        EruptRole eruptRole = new Gson().fromJson(json, EruptRole.class);
        System.out.println(eruptRole.toString());
    }


}

