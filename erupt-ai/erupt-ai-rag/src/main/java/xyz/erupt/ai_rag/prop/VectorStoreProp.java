package xyz.erupt.ai_rag.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_rag.constants.VectorStoreType;

/**
 * Vector store is deployment infrastructure (its SDK must be on the classpath),
 * so it is configured here rather than managed visually like embedding models.
 *
 * @author YuePeng
 * date 2026/8/18
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt.ai.rag.vector-store")
public class VectorStoreProp {

    /**
     * Leave blank to auto-select: the single persistent implementation on the
     * classpath, or MEMORY.
     */
    private VectorStoreType type;

    /**
     * QDRANT: host:6334 (gRPC, https:// prefix for TLS) | MILVUS: http://host:19530
     * | PGVECTOR: postgresql://user:password@host:5432/db (blank = reuse the application datasource;
     * password may be omitted and supplied via api-key) | REDIS: host:6379 (rediss:// prefix for TLS,
     * blank = localhost) | MEMORY: not needed
     */
    private String uri;

    private String apiKey;

}
