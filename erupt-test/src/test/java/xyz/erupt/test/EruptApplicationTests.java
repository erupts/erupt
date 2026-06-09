package xyz.erupt.test;

import jakarta.annotation.Resource;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.erupt.jpa.dao.EruptDao;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EruptApplicationTests {

    @Resource
    protected EruptDao eruptDao;

}
