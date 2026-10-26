package xyz.erupt.test.core;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.PreEruptDataService;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.edit.CheckboxModel;
import xyz.erupt.test.model.edit.RefTargetModel;
import xyz.erupt.test.model.edit.TabTableAddModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The detail path (find by pk, then build the data map) reads lazy to-many collections after the
 * lookup returns. It must work with no surrounding transaction or request-bound EntityManager,
 * i.e. with open-in-view off, which is exactly the situation these tests run in.
 */
public class FindDataByIdTest extends EruptApplicationTests {

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private PreEruptDataService preEruptDataService;

    @Test
    void oneToManyLoadsOutsideTransaction() {
        Long id = transactionTemplate.execute(status -> {
            TabTableAddModel model = new TabTableAddModel();
            model.setChildren(new ArrayList<>(List.of(ref("child-1"), ref("child-2"))));
            eruptDao.persist(model);
            return model.getId();
        });
        Map<String, Object> data = detail(TabTableAddModel.class, id);
        assertEquals(2, ((Collection<?>) data.get("children")).size());
    }

    @Test
    void manyToManyLoadsOutsideTransaction() {
        Long id = transactionTemplate.execute(status -> {
            CheckboxModel model = new CheckboxModel();
            model.setTags(new ArrayList<>(List.of(ref("tag-1"), ref("tag-2"))));
            model.setRoles(new ArrayList<>(List.of(ref("role-1"))));
            eruptDao.persist(model);
            return model.getId();
        });
        Map<String, Object> data = detail(CheckboxModel.class, id);
        assertEquals(2, ((Collection<?>) data.get("tags")).size());
        assertEquals(1, ((Collection<?>) data.get("roles")).size());
    }

    private Map<String, Object> detail(Class<?> clazz, Long id) {
        return preEruptDataService.getEruptData(EruptCoreService.getErupt(clazz.getSimpleName()), id.toString(), false);
    }

    private RefTargetModel ref(String name) {
        RefTargetModel ref = new RefTargetModel();
        ref.setName(name);
        eruptDao.persist(ref);
        return ref;
    }

}
