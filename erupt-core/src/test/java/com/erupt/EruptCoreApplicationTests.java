package com.erupt;

import com.erupt.model.EruptModel;
import com.erupt.service.CoreService;
import com.erupt.util.EruptUtil;
import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EruptCoreApplicationTests {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void contextLoads() {
        EruptModel eruptModel = CoreService.ERUPTS.get("subMmo");
        List<String> fieldNames = EruptUtil.getEruptFieldNames(eruptModel.getEruptFieldModels());
        List list = entityManager.createQuery(" from " + eruptModel.getClazz().getSimpleName()).getResultList();
        System.out.println(new Gson().toJson(list));

    }

}
