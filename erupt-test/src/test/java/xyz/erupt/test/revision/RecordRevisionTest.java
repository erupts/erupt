package xyz.erupt.test.revision;

import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.revision.model.EruptRecordRevision;
import xyz.erupt.revision.model.RevisionOperation;
import xyz.erupt.revision.pojo.FieldChange;
import xyz.erupt.revision.pojo.RevisionVo;
import xyz.erupt.revision.service.EruptRevisionService;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.erupt.AuthVerifyModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * erupt-revision: every change through the erupt pipeline leaves a field-level revision, and an
 * update revision can be rolled back through the same pipeline (which records a revision of its own).
 */
public class RecordRevisionTest extends EruptApplicationTests {

    private static final String ERUPT = AuthVerifyModel.class.getSimpleName();

    @Resource
    private EruptModifyService modifyService;

    @Resource
    private EruptRevisionService revisionService;

    @Resource
    private TransactionTemplate transactionTemplate;

    private Long id;

    @BeforeEach
    void setUp() {
        MetaContext.register(new MetaUser(1L, "admin", "admin"));
        JsonObject data = new JsonObject();
        data.addProperty("key", "rev-key");
        data.addProperty("value", "v1");
        data.addProperty("description", "first");
        this.id = (Long) modifyService.insertEruptData(this.model(), data);
    }

    @AfterEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status -> {
            eruptDao.lambdaQuery(AuthVerifyModel.class).eq(AuthVerifyModel::getKey, "rev-key").delete();
            eruptDao.lambdaQuery(EruptRecordRevision.class).eq(EruptRecordRevision::getErupt, ERUPT).delete();
        });
        MetaContext.remove();
    }

    @Test
    void addUpdateRollbackDelete() {
        String recordId = String.valueOf(id);
        List<RevisionVo> history = revisionService.list(ERUPT, recordId);
        assertEquals(1, history.size());
        assertEquals(RevisionOperation.ADD, history.get(0).getOperation());
        assertEquals(1, history.get(0).getVersion());
        assertEquals("admin", history.get(0).getUserName());
        FieldChange added = history.get(0).getChanges().stream().filter(c -> "value".equals(c.getField())).findFirst().orElseThrow();
        assertNull(added.getBefore());
        assertEquals("v1", added.getAfter().getAsString());

        // one field changed: exactly one entry with both sides, titled by the form title
        JsonObject update = new JsonObject();
        update.addProperty("id", id);
        update.addProperty("key", "rev-key");
        update.addProperty("value", "v2");
        update.addProperty("description", "first");
        modifyService.updateEruptData(this.model(), update);
        history = revisionService.list(ERUPT, recordId);
        assertEquals(2, history.size());
        RevisionVo updated = history.get(0);
        assertEquals(RevisionOperation.UPDATE, updated.getOperation());
        assertEquals(2, updated.getVersion());
        assertEquals(1, updated.getChanges().size());
        FieldChange change = updated.getChanges().get(0);
        assertEquals("value", change.getField());
        assertEquals("Value", change.getTitle());
        assertEquals("v1", change.getBefore().getAsString());
        assertEquals("v2", change.getAfter().getAsString());

        // saving the same values again leaves no trace
        modifyService.updateEruptData(this.model(), update);
        assertEquals(2, revisionService.list(ERUPT, recordId).size());

        // rollback restores the old value and is itself a revision
        revisionService.rollback(ERUPT, recordId, updated.getId());
        assertEquals("v1", eruptDao.find(AuthVerifyModel.class, id).getValue());
        history = revisionService.list(ERUPT, recordId);
        assertEquals(3, history.size());
        assertEquals("v2", history.get(0).getChanges().get(0).getBefore().getAsString());
        assertEquals("v1", history.get(0).getChanges().get(0).getAfter().getAsString());

        // the add revision has no previous state to return to
        Long addRevisionId = history.get(2).getId();
        assertThrows(RuntimeException.class, () -> revisionService.rollback(ERUPT, recordId, addRevisionId));

        modifyService.deleteEruptData(this.model(), List.of(id), false);
        history = revisionService.list(ERUPT, recordId);
        assertEquals(4, history.size());
        assertEquals(RevisionOperation.DELETE, history.get(0).getOperation());
        assertNull(history.get(0).getChanges().stream().filter(c -> "value".equals(c.getField())).findFirst().orElseThrow().getAfter());
    }

    private EruptModel model() {
        return EruptCoreService.getErupt(ERUPT);
    }
}
