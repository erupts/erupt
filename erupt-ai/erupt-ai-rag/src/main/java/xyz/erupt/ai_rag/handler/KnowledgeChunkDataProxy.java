package xyz.erupt.ai_rag.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_rag.model.KnowledgeChunk;
import xyz.erupt.ai_rag.service.RagService;
import xyz.erupt.annotation.fun.DataProxy;

/**
 * @author YuePeng
 * date 2026/8/17
 */
@Component
public class KnowledgeChunkDataProxy implements DataProxy<KnowledgeChunk> {

    @Resource
    private RagService ragService;

    @Override
    public void beforeUpdate(KnowledgeChunk chunk) {
        ragService.reEmbedChunk(chunk);
    }

    @Override
    public void beforeDelete(KnowledgeChunk chunk) {
        ragService.removeChunkVector(chunk);
    }

}
