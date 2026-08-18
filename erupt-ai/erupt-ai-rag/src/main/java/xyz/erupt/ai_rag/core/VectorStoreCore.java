package xyz.erupt.ai_rag.core;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import xyz.erupt.ai_rag.constants.VectorStoreType;
import xyz.erupt.ai_rag.prop.VectorStoreProp;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Vector store type registry. Qdrant/Milvus implementations are registered only
 * when their SDK is on the classpath (guarded by @ConditionalOnClass), so the
 * registry always reflects what the deployment can actually connect to.
 *
 * @author YuePeng
 * date 2026/8/17
 */
public abstract class VectorStoreCore {

    private static final Map<VectorStoreType, VectorStoreCore> registry = new EnumMap<>(VectorStoreType.class);

    public VectorStoreCore() {
        registry.put(this.code(), this);
    }

    public static VectorStoreCore get(VectorStoreType code) {
        return registry.get(code);
    }

    /**
     * Null type auto-selects: the single persistent implementation when exactly
     * one SDK is on the classpath, otherwise the non-persistent MEMORY store.
     * An explicit type whose SDK is absent returns null.
     */
    public static VectorStoreCore resolve(VectorStoreType type) {
        if (null != type) {
            return registry.get(type);
        }
        List<VectorStoreCore> persistent = registry.values().stream()
                .filter(core -> VectorStoreType.MEMORY != core.code()).toList();
        return 1 == persistent.size() ? persistent.get(0) : registry.get(VectorStoreType.MEMORY);
    }

    public abstract VectorStoreType code();

    // Connect to (creating when absent) a collection sized for the given dimension
    public abstract EmbeddingStore<TextSegment> connect(VectorStoreProp config, String collection, int dimension);

    public abstract void dropCollection(VectorStoreProp config, String collection);

    // Throws a descriptive exception when the store is unreachable
    public abstract void healthCheck(VectorStoreProp config);

}
