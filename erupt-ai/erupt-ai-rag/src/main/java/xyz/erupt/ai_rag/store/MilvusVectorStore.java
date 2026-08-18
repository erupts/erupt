package xyz.erupt.ai_rag.store;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_rag.core.VectorStoreCore;
import xyz.erupt.ai_rag.prop.VectorStoreProp;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

/**
 * Milvus. URI format: "http://host:19530". The collection is created on first
 * connect by MilvusEmbeddingStore itself, sized to the embedding dimension.
 *
 * @author YuePeng
 * date 2026/8/17
 */
@Component
@ConditionalOnClass(MilvusServiceClient.class)
public class MilvusVectorStore extends VectorStoreCore {

    public static final String CODE = "MILVUS";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public EmbeddingStore<TextSegment> connect(VectorStoreProp config, String collection, int dimension) {
        MilvusEmbeddingStore.Builder builder = MilvusEmbeddingStore.builder()
                .uri(config.getUri())
                .collectionName(collection)
                .dimension(dimension);
        if (StringUtils.isNotBlank(config.getApiKey())) {
            builder.token(config.getApiKey());
        }
        return builder.build();
    }

    @Override
    public void dropCollection(VectorStoreProp config, String collection) {
        // Dimension only matters when the collection is missing, in which case
        // connect() creates it and the drop below removes it right away.
        ((MilvusEmbeddingStore) this.connect(config, collection, 4)).dropCollection(collection);
    }

    @Override
    public void healthCheck(VectorStoreProp config) {
        ConnectParam.Builder param = ConnectParam.newBuilder().withUri(config.getUri());
        if (StringUtils.isNotBlank(config.getApiKey())) {
            param.withToken(config.getApiKey());
        }
        MilvusServiceClient client = new MilvusServiceClient(param.build());
        try {
            io.milvus.param.R<?> resp = client.checkHealth();
            if (resp.getStatus() != io.milvus.param.R.Status.Success.getCode()) {
                throw new EruptWebApiRuntimeException("Milvus health check failed: " + resp.getMessage());
            }
        } finally {
            client.close();
        }
    }

}
