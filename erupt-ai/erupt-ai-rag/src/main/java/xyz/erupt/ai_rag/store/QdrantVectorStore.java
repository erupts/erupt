package xyz.erupt.ai_rag.store;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_rag.constants.VectorStoreType;
import xyz.erupt.ai_rag.core.VectorStoreCore;
import xyz.erupt.ai_rag.prop.VectorStoreProp;

import java.util.concurrent.TimeUnit;

/**
 * Qdrant over gRPC. URI format: "host:6334" or "https://host:6334" (TLS).
 *
 * @author YuePeng
 * date 2026/8/17
 */
@Component
@ConditionalOnClass(QdrantClient.class)
public class QdrantVectorStore extends VectorStoreCore {

    private static final int DEFAULT_GRPC_PORT = 6334;

    @Override
    public VectorStoreType code() {
        return VectorStoreType.QDRANT;
    }

    @Override
    @SneakyThrows
    public EmbeddingStore<TextSegment> connect(VectorStoreProp config, String collection, int dimension) {
        Address address = Address.parse(config.getUri());
        try (QdrantClient client = client(address, config.getApiKey())) {
            if (!client.collectionExistsAsync(collection).get(30, TimeUnit.SECONDS)) {
                client.createCollectionAsync(collection, Collections.VectorParams.newBuilder()
                        .setDistance(Collections.Distance.Cosine).setSize(dimension).build()).get(30, TimeUnit.SECONDS);
            }
        }
        QdrantEmbeddingStore.Builder builder = QdrantEmbeddingStore.builder()
                .host(address.host).port(address.port).useTls(address.tls)
                .collectionName(collection);
        if (StringUtils.isNotBlank(config.getApiKey())) {
            builder.apiKey(config.getApiKey());
        }
        return builder.build();
    }

    @Override
    @SneakyThrows
    public void dropCollection(VectorStoreProp config, String collection) {
        try (QdrantClient client = client(Address.parse(config.getUri()), config.getApiKey())) {
            client.deleteCollectionAsync(collection).get(30, TimeUnit.SECONDS);
        }
    }

    @Override
    @SneakyThrows
    public void healthCheck(VectorStoreProp config) {
        try (QdrantClient client = client(Address.parse(config.getUri()), config.getApiKey())) {
            client.listCollectionsAsync().get(10, TimeUnit.SECONDS);
        }
    }

    private QdrantClient client(Address address, String apiKey) {
        QdrantGrpcClient.Builder builder = QdrantGrpcClient.newBuilder(address.host, address.port, address.tls);
        if (StringUtils.isNotBlank(apiKey)) {
            builder.withApiKey(apiKey);
        }
        return new QdrantClient(builder.build());
    }

    private record Address(String host, int port, boolean tls) {
        static Address parse(String uri) {
            boolean tls = uri.startsWith("https://");
            String hostPort = uri.replaceFirst("^https?://", "");
            String[] parts = hostPort.split(":");
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : DEFAULT_GRPC_PORT;
            return new Address(parts[0], port, tls);
        }
    }

}
