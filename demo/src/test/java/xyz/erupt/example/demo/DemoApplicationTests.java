package xyz.erupt.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.service.data_impl.DBService;
import xyz.erupt.example.demo.model.Product;
import xyz.erupt.example.demo.model.ProductParam;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @PersistenceContext
    private EntityManager entityManager;


    @Autowired
    private DBService dbService;

    @Test
    public void findEruptDataById() {
        Object obj = dbService.findDataById(CoreService.getErupt("EruptRole"), 1);
        System.out.println(obj);
    }

    @Test
    public void test() {
        Product product = new Product();
        product.setName("p1");
        Set<ProductParam> set = new HashSet<>();
        set.add(new ProductParam() {{
            this.setFloat1(1D);
            this.setProduct(product);
            this.setName("pm1");
        }});
        product.setProductParam(set);
        entityManager.persist(product);
    }

}

