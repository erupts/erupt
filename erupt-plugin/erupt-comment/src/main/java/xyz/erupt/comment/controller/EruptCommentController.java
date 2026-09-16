package xyz.erupt.comment.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.comment.pojo.CommentInput;
import xyz.erupt.comment.pojo.CommentVo;
import xyz.erupt.comment.pojo.MentionVo;
import xyz.erupt.comment.service.EruptCommentService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;

import java.util.List;
import java.util.Map;

/**
 * Comments of one record. Every route carries the model name at path index 1, so the
 * erupt permission check applies: whoever may open the model may read and write its comments.
 * The literal segments (counts, mention-users) take precedence over the record id variable.
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/comment")
public class EruptCommentController {

    @Resource
    private EruptCommentService eruptCommentService;

    @GetMapping("/{erupt}/{id}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public R<List<CommentVo>> list(@PathVariable("erupt") String erupt, @PathVariable("id") String id) {
        return R.ok(eruptCommentService.list(erupt, id));
    }

    // comment count per record for the rows of one table page: body is the list of record ids
    @PostMapping("/{erupt}/counts")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public R<Map<String, Long>> counts(@PathVariable("erupt") String erupt, @RequestBody List<String> ids) {
        return R.ok(eruptCommentService.counts(erupt, ids));
    }

    @GetMapping("/{erupt}/mention-users")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public R<List<MentionVo>> mentionUsers(@PathVariable("erupt") String erupt,
                                           @RequestParam(value = "keyword", required = false) String keyword) {
        return R.ok(eruptCommentService.mentionCandidates(keyword));
    }

    @PostMapping("/{erupt}/{id}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public R<CommentVo> add(@PathVariable("erupt") String erupt, @PathVariable("id") String id, @RequestBody CommentInput input) {
        return R.ok(eruptCommentService.add(erupt, id, input));
    }

    @PutMapping("/{erupt}/{id}/{commentId}/resolved")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public R<CommentVo> resolved(@PathVariable("erupt") String erupt, @PathVariable("id") String id,
                                 @PathVariable("commentId") Long commentId, @RequestParam("value") boolean value) {
        return R.ok(eruptCommentService.setResolved(erupt, id, commentId, value));
    }

    @PutMapping("/{erupt}/{id}/{commentId}/pinned")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public R<CommentVo> pinned(@PathVariable("erupt") String erupt, @PathVariable("id") String id,
                               @PathVariable("commentId") Long commentId, @RequestParam("value") boolean value) {
        return R.ok(eruptCommentService.setPinned(erupt, id, commentId, value));
    }

    @DeleteMapping("/{erupt}/{id}/{commentId}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public R<Void> delete(@PathVariable("erupt") String erupt, @PathVariable("id") String id, @PathVariable("commentId") Long commentId) {
        eruptCommentService.delete(erupt, id, commentId);
        return R.ok();
    }
}
