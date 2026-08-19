package xyz.erupt.ai_rag.store;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.VSimParams;

import java.util.*;
import java.util.stream.Collectors;

/**
 * EmbeddingStore over Redis 8 vector sets: one vector set key per collection,
 * element = vector id. VSIM scores are (1 + cosine) / 2, the same scale
 * langchain4j uses for relevance, so minScore maps through unchanged.
 * Segment text is not stored — chunk text lives in the erupt database.
 *
 * @author YuePeng
 * date 2026/8/19
 */
public class RedisVectorSetEmbeddingStore implements EmbeddingStore<TextSegment> {

    // VSIM COUNT must be finite; far above any sane topK
    private static final int MAX_RESULTS_CAP = 10_000;

    private final UnifiedJedis jedis;

    private final String key;

    public RedisVectorSetEmbeddingStore(UnifiedJedis jedis, String key) {
        this.jedis = jedis;
        this.key = key;
    }

    @Override
    public String add(Embedding embedding) {
        String id = UUID.randomUUID().toString();
        this.add(id, embedding);
        return id;
    }

    @Override
    public void add(String id, Embedding embedding) {
        jedis.vadd(key, embedding.vector(), id);
    }

    @Override
    public String add(Embedding embedding, TextSegment segment) {
        return this.add(embedding);
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        List<String> ids = new ArrayList<>(embeddings.size());
        for (Embedding embedding : embeddings) {
            ids.add(this.add(embedding));
        }
        return ids;
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings, List<TextSegment> segments) {
        return this.addAll(embeddings);
    }

    @Override
    public void remove(String id) {
        jedis.vrem(key, id);
    }

    @Override
    public void removeAll(Collection<String> ids) {
        ids.forEach(id -> jedis.vrem(key, id));
    }

    @Override
    public void removeAll() {
        jedis.del(key);
    }

    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        Map<String, Double> scores = jedis.vsimWithScores(key, request.queryEmbedding().vector(),
                new VSimParams().count(Math.min(request.maxResults(), MAX_RESULTS_CAP)));
        List<EmbeddingMatch<TextSegment>> matches = scores.entrySet().stream()
                .filter(entry -> entry.getValue() >= request.minScore())
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(entry -> new EmbeddingMatch<TextSegment>(entry.getValue(), entry.getKey(), null, null))
                .collect(Collectors.toList());
        return new EmbeddingSearchResult<>(matches);
    }

}
