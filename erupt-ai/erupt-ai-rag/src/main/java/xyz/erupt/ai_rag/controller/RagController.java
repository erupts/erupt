package xyz.erupt.ai_rag.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.ai_rag.core.RetrievedChunk;
import xyz.erupt.ai_rag.model.KnowledgeBase;
import xyz.erupt.ai_rag.service.RagService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;

/**
 * Backs the "Retrieval Test" tpl page on the knowledge base list.
 *
 * @author YuePeng
 * date 2026/8/17
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/rag")
public class RagController {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private RagService ragService;

    @GetMapping("/retrieve")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public R<List<RetrievedChunk>> retrieve(@RequestParam("kbId") Long kbId,
                                            @RequestParam("query") String query,
                                            @RequestParam(value = "topK", required = false) Integer topK,
                                            @RequestParam(value = "minScore", required = false) Double minScore) {
        KnowledgeBase kb = eruptDao.find(KnowledgeBase.class, kbId);
        if (null == kb) {
            throw new EruptWebApiRuntimeException("Knowledge base not found: " + kbId);
        }
        return R.ok(ragService.retrieve(kb, query, topK, minScore));
    }

}
