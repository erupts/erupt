package xyz.erupt.ai_rag.store;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_rag.constants.VectorStoreType;
import xyz.erupt.ai_rag.core.VectorStoreCore;
import xyz.erupt.ai_rag.prop.VectorStoreProp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process, non-persistent store. Vectors are lost on restart — meant for
 * demos and tests; re-embed documents after a restart if you use it anyway.
 *
 * @author YuePeng
 * date 2026/8/17
 */
@Component
public class MemoryVectorStore extends VectorStoreCore {

    private static final Map<String, InMemoryEmbeddingStore<TextSegment>> stores = new ConcurrentHashMap<>();

    @Override
    public VectorStoreType code() {
        return VectorStoreType.MEMORY;
    }

    @Override
    public EmbeddingStore<TextSegment> connect(VectorStoreProp config, String collection, int dimension) {
        return stores.computeIfAbsent(collection, k -> new InMemoryEmbeddingStore<>());
    }

    @Override
    public void dropCollection(VectorStoreProp config, String collection) {
        stores.remove(collection);
    }

    @Override
    public void healthCheck(VectorStoreProp config) {
        // Always healthy
    }

}
