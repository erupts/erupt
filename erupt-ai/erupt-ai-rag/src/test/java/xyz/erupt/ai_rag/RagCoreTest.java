package xyz.erupt.ai_rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;
import xyz.erupt.ai_rag.constants.VectorStoreType;
import xyz.erupt.ai_rag.core.VectorStoreCore;
import xyz.erupt.ai_rag.store.MemoryVectorStore;
import xyz.erupt.ai_rag.store.RedisVectorSetEmbeddingStore;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author YuePeng
 * date 2026/8/17
 */
public class RagCoreTest {

    @Test
    public void vectorStoreRegistry() {
        MemoryVectorStore store = new MemoryVectorStore();
        assertSame(store, VectorStoreCore.get(VectorStoreType.MEMORY));
        // Null type auto-selects MEMORY when no persistent SDK is registered
        assertSame(store, VectorStoreCore.resolve(null));
        // An explicit type whose SDK is absent resolves to nothing instead of silently falling back
        assertNull(VectorStoreCore.resolve(VectorStoreType.QDRANT));
    }

    @Test
    public void memoryStoreRoundTrip() {
        MemoryVectorStore factory = new MemoryVectorStore();
        EmbeddingStore<TextSegment> store = factory.connect(null, "test_collection", 3);
        // Same collection name must return the same store instance
        assertSame(store, factory.connect(null, "test_collection", 3));

        String alphaId = store.add(Embedding.from(new float[]{1f, 0f, 0f}), TextSegment.from("alpha"));
        store.add(Embedding.from(new float[]{0f, 1f, 0f}), TextSegment.from("beta"));

        EmbeddingSearchResult<TextSegment> result = store.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(Embedding.from(new float[]{0.9f, 0.1f, 0f})).maxResults(1).build());
        assertEquals(1, result.matches().size());
        assertEquals("alpha", result.matches().get(0).embedded().text());
        assertEquals(alphaId, result.matches().get(0).embeddingId());
        assertTrue(result.matches().get(0).score() > 0.9);

        // Removing the vector must make it unretrievable — the re-embed flow relies on this
        store.remove(alphaId);
        EmbeddingSearchResult<TextSegment> afterRemove = store.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(Embedding.from(new float[]{1f, 0f, 0f})).maxResults(10).build());
        assertEquals(1, afterRemove.matches().size());
        assertEquals("beta", afterRemove.matches().get(0).embedded().text());

        factory.dropCollection(null, "test_collection");
    }

    // Runs against a local Redis 8; skipped when none is reachable or vector sets are unsupported.
    // Uses the store class directly so the static VectorStoreCore registry stays MEMORY-only for other tests.
    @Test
    public void redisVectorSetRoundTrip() {
        try (JedisPooled jedis = new JedisPooled(new HostAndPort("localhost", 6379), DefaultJedisClientConfig.builder().build())) {
            try {
                jedis.vcard("__erupt_vector_probe__");
            } catch (Exception e) {
                Assumptions.assumeTrue(false, "local redis with vector set support unavailable: " + e.getMessage());
            }
            String key = "erupt_test_vector_set";
            jedis.del(key);
            EmbeddingStore<TextSegment> store = new RedisVectorSetEmbeddingStore(jedis, key);

            String alphaId = store.add(Embedding.from(new float[]{1f, 0f, 0f}), TextSegment.from("alpha"));
            store.add(Embedding.from(new float[]{0f, 1f, 0f}), TextSegment.from("beta"));

            EmbeddingSearchResult<TextSegment> result = store.search(EmbeddingSearchRequest.builder()
                    .queryEmbedding(Embedding.from(new float[]{0.9f, 0.1f, 0f})).maxResults(1).build());
            assertEquals(1, result.matches().size());
            assertEquals(alphaId, result.matches().get(0).embeddingId());
            assertTrue(result.matches().get(0).score() > 0.9);

            // minScore filters on the same (1 + cosine) / 2 scale the rest of the module uses
            EmbeddingSearchResult<TextSegment> filtered = store.search(EmbeddingSearchRequest.builder()
                    .queryEmbedding(Embedding.from(new float[]{1f, 0f, 0f})).maxResults(10).minScore(0.9).build());
            assertEquals(1, filtered.matches().size());

            store.remove(alphaId);
            EmbeddingSearchResult<TextSegment> afterRemove = store.search(EmbeddingSearchRequest.builder()
                    .queryEmbedding(Embedding.from(new float[]{1f, 0f, 0f})).maxResults(10).build());
            assertEquals(1, afterRemove.matches().size());
            assertNotEquals(alphaId, afterRemove.matches().get(0).embeddingId());

            jedis.del(key);
        }
    }

}
