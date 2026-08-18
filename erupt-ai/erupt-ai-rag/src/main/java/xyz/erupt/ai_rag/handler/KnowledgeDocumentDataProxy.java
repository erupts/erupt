package xyz.erupt.ai_rag.handler;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_rag.constants.DocStatus;
import xyz.erupt.ai_rag.model.KnowledgeDocument;
import xyz.erupt.ai_rag.service.RagService;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;
import java.util.Objects;

/**
 * @author YuePeng
 * date 2026/8/17
 */
@Component
public class KnowledgeDocumentDataProxy implements DataProxy<KnowledgeDocument>, OperationHandler<KnowledgeDocument, Void> {

    // Whether the pending update touched the embedded content (decided in beforeUpdate, consumed in afterUpdate)
    private static final ThreadLocal<Boolean> CONTENT_CHANGED = new ThreadLocal<>();

    @Resource
    private EruptDao eruptDao;

    @Resource
    private RagService ragService;

    @Override
    public void beforeAdd(KnowledgeDocument doc) {
        this.validateSource(doc);
        doc.setStatus(DocStatus.PENDING);
        doc.setChunkCount(null);
        doc.setErrorInfo(null);
    }

    @Override
    public void afterAdd(KnowledgeDocument doc) {
        ragService.ingestAsync(doc.getId());
    }

    @Override
    public void beforeUpdate(KnowledgeDocument doc) {
        this.validateSource(doc);
        KnowledgeDocument old = eruptDao.find(KnowledgeDocument.class, doc.getId());
        boolean changed = !Objects.equals(old.getAttachment(), doc.getAttachment())
                || !Objects.equals(old.getContent(), doc.getContent());
        eruptDao.detach(old);
        CONTENT_CHANGED.set(changed);
        if (changed) {
            doc.setStatus(DocStatus.PENDING);
        }
    }

    // Renaming a document must not pay for a full re-embedding, only content changes do
    @Override
    public void afterUpdate(KnowledgeDocument doc) {
        Boolean changed = CONTENT_CHANGED.get();
        CONTENT_CHANGED.remove();
        if (Boolean.TRUE.equals(changed)) {
            ragService.ingestAsync(doc.getId());
        }
    }

    @Override
    public void beforeDelete(KnowledgeDocument doc) {
        ragService.removeDocumentData(eruptDao.find(KnowledgeDocument.class, doc.getId()));
    }

    // "Re-embed" row operation
    @Override
    public String exec(List<KnowledgeDocument> data, Void unused, String[] param) {
        data.forEach(doc -> ragService.ingestAsync(doc.getId()));
        return "";
    }

    private void validateSource(KnowledgeDocument doc) {
        if (StringUtils.isBlank(doc.getAttachment()) && StringUtils.isBlank(doc.getContent())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("rag.empty_content"));
        }
    }

}
