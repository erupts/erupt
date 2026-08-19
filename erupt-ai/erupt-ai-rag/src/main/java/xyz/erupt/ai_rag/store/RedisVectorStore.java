package xyz.erupt.ai_rag.store;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;
import xyz.erupt.ai_rag.constants.VectorStoreType;
import xyz.erupt.ai_rag.core.VectorStoreCore;
import xyz.erupt.ai_rag.prop.VectorStoreProp;

/**
 * Redis 8 vector sets (core data type, no RediSearch module required).
 * URI format: "host:6379" or "rediss://host:6379" (TLS); blank = localhost:6379.
 *
 * @author YuePeng
 * date 2026/8/19
 */
@Component
@ConditionalOnClass(UnifiedJedis.class)
public class RedisVectorStore extends VectorStoreCore {

    private static final int DEFAULT_PORT = 6379;

    @Override
    public VectorStoreType code() {
        return VectorStoreType.REDIS;
    }

    @Override
    public EmbeddingStore<TextSegment> connect(VectorStoreProp config, String collection, int dimension) {
        // A vector set sizes itself on the first VADD, no explicit creation step
        return new RedisVectorSetEmbeddingStore(this.client(config), collection);
    }

    @Override
    public void dropCollection(VectorStoreProp config, String collection) {
        try (UnifiedJedis jedis = this.client(config)) {
            jedis.del(collection);
        }
    }

    @Override
    public void healthCheck(VectorStoreProp config) {
        try (UnifiedJedis jedis = this.client(config)) {
            // VCARD on a missing key is 0 on Redis 8+ and an unknown-command error below it,
            // so this probes vector set capability, not just liveness
            jedis.vcard("__erupt_vector_probe__");
        }
    }

    private UnifiedJedis client(VectorStoreProp config) {
        String uri = StringUtils.defaultIfBlank(config.getUri(), "localhost:" + DEFAULT_PORT);
        boolean tls = uri.startsWith("rediss://");
        String hostPort = uri.replaceFirst("^rediss?://", "");
        String[] parts = hostPort.split(":");
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : DEFAULT_PORT;
        DefaultJedisClientConfig.Builder builder = DefaultJedisClientConfig.builder().ssl(tls);
        if (StringUtils.isNotBlank(config.getApiKey())) {
            builder.password(config.getApiKey());
        }
        return new JedisPooled(new HostAndPort(parts[0], port), builder.build());
    }

}
