package xyz.erupt.revision.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.revision.service.EruptRevisionService;
import xyz.erupt.upms.service.EruptUserService;

/**
 * Record history as AI tools: an agent that changes data can see what a record looked like before
 * and put it back. Reading asks for the model's menu, as the history panel does; a rollback goes
 * through the regular update pipeline and a restore through the regular add pipeline, so the edit
 * and add powers of the model are checked where they always are.
 *
 * @author YuePeng
 */
@AiToolbox
@Component
public class RevisionTools {

    private static final String ERUPT_HINT = "Erupt model name the record belongs to";

    private static final String RECORD_HINT = "Primary key of the record";

    @Resource
    private EruptRevisionService eruptRevisionService;

    @Resource
    private EruptUserService eruptUserService;

    @Tool("List the revisions of one record, newest first: who changed it, when, the operation (ADD, UPDATE, DELETE) "
            + "and every field's value before and after. Works for a deleted record too.")
    public String listRecordRevisions(@P(ERUPT_HINT) String eruptName, @P(RECORD_HINT) String recordId) {
        eruptUserService.requireAiModel(eruptName);
        return GsonFactory.getGson().toJson(eruptRevisionService.list(eruptName, recordId));
    }

    @Tool("Undo one UPDATE revision of a record: every field that revision changed gets its earlier value back, "
            + "other fields keep their current value. The rollback is itself recorded as a new revision.")
    public String rollbackRecordRevision(@P(ERUPT_HINT) String eruptName, @P(RECORD_HINT) String recordId,
                                         @P("Id of the revision to undo, from listRecordRevisions") Long revisionId) {
        eruptUserService.requireAiModel(eruptName);
        eruptRevisionService.rollback(eruptName, recordId, revisionId);
        return GsonFactory.getGson().toJson(eruptRevisionService.list(eruptName, recordId).get(0));
    }

    @Tool("Bring a deleted record back from its last revision. Returns the primary key it now has, "
            + "which differs from the old one when the key is generated.")
    public String restoreDeletedRecord(@P(ERUPT_HINT) String eruptName,
                                       @P("Primary key the record had before it was deleted") String recordId) {
        eruptUserService.requireAiModel(eruptName);
        return String.valueOf(eruptRevisionService.restore(eruptName, recordId));
    }

}
