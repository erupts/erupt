package xyz.erupt.revision.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
import xyz.erupt.revision.pojo.RevisionVo;
import xyz.erupt.revision.service.EruptRevisionService;

import java.util.List;

/**
 * Revision history of one record. Every route carries the model name at path index 1, so the
 * erupt permission check applies: whoever may open the model may read its history, and a
 * rollback additionally requires edit power on the model.
 * <p>
 * cloudProxy = false keeps these routes on the server even when the model belongs to an
 * erupt-cloud node: the revision rows live in the server's own database, keyed by
 * "nodeName.eruptName" exactly as the model name arrives here.
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/revision")
public class EruptRevisionController {

    @Resource
    private EruptRevisionService eruptRevisionService;

    @GetMapping("/{erupt}/{id}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, cloudProxy = false)
    public R<List<RevisionVo>> list(@PathVariable("erupt") String erupt, @PathVariable("id") String id) {
        return R.ok(eruptRevisionService.list(erupt, id));
    }

    @PostMapping("/{erupt}/{id}/{revisionId}/rollback")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT, cloudProxy = false)
    public R<Void> rollback(@PathVariable("erupt") String erupt, @PathVariable("id") String id,
                            @PathVariable("revisionId") Long revisionId) {
        eruptRevisionService.rollback(erupt, id, revisionId);
        return R.ok();
    }
}
