package xyz.erupt.test.revision;

import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.revision.model.EruptRecordRevision;
import xyz.erupt.revision.model.RevisionOperation;
import xyz.erupt.revision.pojo.RevisionVo;
import xyz.erupt.revision.service.EruptRevisionService;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.erupt.AuthVerifyModel;
import xyz.erupt.test.model.erupt.OrderByModel;
import xyz.erupt.upms.service.EruptTokenService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The revision table as recycle bin: a deleted record shows up with a summary, comes back through
 * the add pipeline with its history following it, and leaves the bin when its delete revision is deleted.
 */
class RevisionRestoreTest extends EruptApplicationTests {

    private static final String ERUPT = AuthVerifyModel.class.getSimpleName();

    private static final String KEY = "bin-key";

    private static final String ORDERS = OrderByModel.class.getSimpleName();

    @Resource
    private EruptModifyService modifyService;

    @Resource
    private EruptRevisionService revisionService;

    @Resource
    private EruptService eruptService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private EruptTokenService eruptTokenService;

    private Long id;

    @BeforeEach
    void setUp() {
        // the recycle bin page is read as a logged-in user whose menus include it
        MetaUserinfo user = new MetaUserinfo();
        user.setId(-90L);
        user.setAccount("bin-user");
        user.setUsername("bin-user");
        String token = Erupts.generateCode(22);
        // the revision table, and the permission-checked model with its add and delete buttons for the second test
        MetaMenu orders = MetaMenu.createEruptClassMenu(OrderByModel.class, null, 2);
        eruptTokenService.loginToken(user, List.of(MetaMenu.createEruptClassMenu(EruptRecordRevision.class, null, 1), orders,
                MetaMenu.createSimpleMenu("orders-add", "Add", ORDERS + "@ADD", orders, 1, "button"),
                MetaMenu.createSimpleMenu("orders-delete", "Delete", ORDERS + "@DELETE", orders, 2, "button")), token, 5);
        MetaContext.register(new MetaUser(-90L, "bin-user", "bin-user"));
        MetaContext.registerToken(token);
        this.cleanUp();
        JsonObject data = new JsonObject();
        data.addProperty("key", KEY);
        data.addProperty("value", "v1");
        data.addProperty("description", "to be deleted");
        this.id = (Long) modifyService.insertEruptData(this.model(), data);
        data.addProperty("id", id);
        data.addProperty("value", "v2");
        modifyService.updateEruptData(this.model(), data);
        modifyService.deleteEruptData(this.model(), List.of(id), false);
    }

    @AfterEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status -> {
            eruptDao.lambdaQuery(AuthVerifyModel.class).eq(AuthVerifyModel::getKey, KEY).delete();
            eruptDao.lambdaQuery(OrderByModel.class).eq(OrderByModel::getName, KEY).delete();
            eruptDao.lambdaQuery(EruptRecordRevision.class).eq(EruptRecordRevision::getErupt, ERUPT).delete();
            eruptDao.lambdaQuery(EruptRecordRevision.class).eq(EruptRecordRevision::getErupt, ORDERS).delete();
        });
    }

    @AfterEach
    void tearDown() {
        MetaContext.remove();
    }

    private EruptModel model() {
        return EruptCoreService.getErupt(ERUPT);
    }

    // the revision page filtered on deletes of this model, through the same path the table uses
    private List<Map<String, Object>> bin() {
        MetaContext.register(new MetaErupt(EruptRecordRevision.class.getSimpleName()));
        TableQuery query = new TableQuery();
        query.setPageIndex(1);
        query.setPageSize(100);
        query.setCondition(List.of(new Condition("erupt", ERUPT, QueryExpression.EQ),
                new Condition("operation", RevisionOperation.DELETE.name(), QueryExpression.EQ)));
        return List.copyOf(eruptService.getEruptData(EruptCoreService.getErupt(EruptRecordRevision.class.getSimpleName()), query, null).getList());
    }

    @Test
    void aDeletedRecordIsListedRestoredAndItsHistoryFollows() {
        List<Map<String, Object>> rows = this.bin();
        assertEquals(1, rows.size(), "the delete revision is the record's last word");
        assertEquals(String.valueOf(id), rows.get(0).get("recordId"));
        String summary = String.valueOf(rows.get(0).get("summary"));
        assertTrue(summary.contains(KEY), "the summary reads from the snapshot");
        assertFalse(summary.contains("("), "not restored yet");

        Long restored = (Long) revisionService.restore(ERUPT, String.valueOf(id));
        AuthVerifyModel row = eruptDao.find(AuthVerifyModel.class, restored);
        assertEquals(KEY, row.getKey());
        assertEquals("v2", row.getValue(), "the last state before the delete comes back");
        rows = this.bin();
        assertEquals(1, rows.size(), "the delete revision stays in the history");
        assertEquals(String.valueOf(restored), rows.get(0).get("recordId"), "under the restored id");
        assertTrue(String.valueOf(rows.get(0).get("summary")).endsWith(")"), "and is marked as restored");

        List<RevisionVo> history = revisionService.list(ERUPT, String.valueOf(restored));
        assertEquals(List.of(RevisionOperation.ADD, RevisionOperation.DELETE, RevisionOperation.UPDATE, RevisionOperation.ADD),
                history.stream().map(RevisionVo::getOperation).toList(), "the old history moved under the restored id");
        assertEquals(List.of(4, 3, 2, 1), history.stream().map(RevisionVo::getVersion).toList());
        if (!restored.equals(id)) assertTrue(revisionService.list(ERUPT, String.valueOf(id)).isEmpty());

        assertThrows(RuntimeException.class, () -> revisionService.restore(ERUPT, String.valueOf(restored)), "nothing to restore while the record exists");
    }

    /**
     * The buttons are pressed on the revision page, whose own model allows neither add nor delete:
     * the power check must be made against the record's model, where this user holds both buttons.
     */
    @Test
    void powerIsCheckedOnTheRecordsModelNotTheBinPage() {
        EruptModel orders = EruptCoreService.getErupt(ORDERS);
        JsonObject data = new JsonObject();
        data.addProperty("name", KEY);
        data.addProperty("sort", 1);
        Long orderId = (Long) modifyService.insertEruptData(orders, data);
        modifyService.deleteEruptData(orders, List.of(orderId), false);

        MetaContext.register(new MetaErupt(EruptRecordRevision.class.getSimpleName()));
        Long restored = (Long) revisionService.restore(ORDERS, String.valueOf(orderId));
        assertEquals(KEY, eruptDao.find(OrderByModel.class, restored).getName());
        assertEquals(EruptRecordRevision.class.getSimpleName(), MetaContext.getErupt().getName(), "the page's context is put back");

    }

    @Test
    void deletingTheDeleteRevisionEmptiesTheBin() {
        EruptModel revisions = EruptCoreService.getErupt(EruptRecordRevision.class.getSimpleName());
        Long deleteRevisionId = revisionService.list(ERUPT, String.valueOf(id)).get(0).getId();
        modifyService.deleteEruptData(revisions, List.of(deleteRevisionId), false);
        assertTrue(this.bin().isEmpty());
        assertEquals(2, revisionService.list(ERUPT, String.valueOf(id)).size(), "the add and update stay as history");
        assertThrows(RuntimeException.class, () -> revisionService.restore(ERUPT, String.valueOf(id)), "nothing left to restore from");
    }

}
