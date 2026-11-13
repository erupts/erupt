package xyz.erupt.ai_rag.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_rag.model.KnowledgeChunk;
import xyz.erupt.ai_rag.scope.KbScope;
import xyz.erupt.ai_rag.service.RagService;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.scope.EruptScopes;

import java.util.List;

/**
 * @author YuePeng
 * date 2026/8/17
 */
@Component
public class KnowledgeChunkDataProxy implements DataProxy<KnowledgeChunk> {

    @Resource
    private RagService ragService;

    // the model binds nothing by name, so the picked knowledge base is added by hand, through the
    // document: no kb column on a chunk, and existing rows need no backfill
    @Override
    public String beforeFetch(List<Condition> conditions) {
        Long kb = EruptScopes.get(KbScope.class).getKb();
        if (null != kb) conditions.add(new Condition("document.kb.id", kb, QueryExpression.EQ));
        return null;
    }

    @Override
    public void beforeUpdate(KnowledgeChunk chunk) {
        ragService.reEmbedChunk(chunk);
    }

    @Override
    public void beforeDelete(KnowledgeChunk chunk) {
        ragService.removeChunkVector(chunk);
    }

}
