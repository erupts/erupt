package xyz.erupt.comment.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.comment.pojo.CommentInput;
import xyz.erupt.comment.service.EruptCommentService;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.upms.service.EruptUserService;

import java.util.List;

/**
 * Record comments as AI tools, so an agent can read the discussion on a record and leave a trace
 * of what it did there. The right is the one the comment routes ask for: whoever may open the
 * model may read and write its comments.
 *
 * @author YuePeng
 */
@AiToolbox
@Component
public class CommentTools {

    private static final String ERUPT_HINT = "Erupt model name the record belongs to";

    private static final String RECORD_HINT = "Primary key of the record";

    @Resource
    private EruptCommentService eruptCommentService;

    @Resource
    private EruptUserService eruptUserService;

    @Tool("List the comments on one record, oldest first. A reply carries the parentId of the thread head it answers; "
            + "a thread head may be resolved or pinned.")
    public String listRecordComments(@P(ERUPT_HINT) String eruptName, @P(RECORD_HINT) String recordId) {
        eruptUserService.requireAiModel(eruptName);
        return GsonFactory.getGson().toJson(eruptCommentService.list(eruptName, recordId));
    }

    @Tool("Comment on one record as the current user. Mentioned users are notified.")
    public String addRecordComment(@P(ERUPT_HINT) String eruptName, @P(RECORD_HINT) String recordId,
                                   @P("Comment text") String content,
                                   @P(value = "Id of the comment to reply to, from listRecordComments; omit to start a new thread", required = false) Long parentId,
                                   @P(value = "Ids of the users to mention, from findUsers", required = false) List<Long> mentions) {
        eruptUserService.requireAiModel(eruptName);
        CommentInput input = new CommentInput();
        input.setContent(content);
        input.setParentId(parentId);
        input.setMentions(mentions);
        return GsonFactory.getGson().toJson(eruptCommentService.add(eruptName, recordId, input));
    }

}
