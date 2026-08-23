package xyz.erupt.ai_rag.handler;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_rag.model.KnowledgeBase;
import xyz.erupt.ai_rag.model.KnowledgeDocument;
import xyz.erupt.ai_rag.service.RagService;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

/**
 * @author YuePeng
 * date 2026/8/17
 */
@Slf4j
@Component
public class KnowledgeBaseDataProxy implements DataProxy<KnowledgeBase> {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private RagService ragService;

    @Override
    public void beforeDelete(KnowledgeBase kb) {
        if (eruptDao.lambdaQuery(KnowledgeDocument.class).eq(KnowledgeDocument::getKb, kb).count() > 0) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("rag.kb_has_documents"));
        }
        try {
            ragService.dropKnowledgeBase(kb);
        } catch (Exception e) {
            // An unreachable store must not block the delete; the empty collection is orphaned at worst
            log.warn("Failed to drop vector collection of knowledge base {}: {}", kb.getId(), e.getMessage());
        }
    }

    @Override
    public void afterUpdate(KnowledgeBase kb) {
        ragService.evictStore(kb.getId());
    }

}
