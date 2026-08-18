package xyz.erupt.ai_rag.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
     * Store type code (QDRANT / MILVUS / MEMORY). Leave blank to auto-select:
     * the single persistent implementation on the classpath, or MEMORY.
     */
    private String type;

    /**
     * QDRANT: host:6334 (gRPC, https:// prefix for TLS) | MILVUS: http://host:19530 | MEMORY: not needed
     */
    private String uri;

    private String apiKey;

}
