package xyz.erupt.ai.service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import xyz.erupt.ai.core.EmbeddingCore;
import xyz.erupt.ai.model.EmbeddingLLM;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches one {@link EmbeddingModel} instance per config with hot-swap eviction,
 * so consumers never rebuild HTTP clients per call.
 *
 * @author YuePeng
 * date 2026/8/17
 */
@Service
public class EmbeddingModelService {

    private record Entry(String fingerprint, EmbeddingModel model) {
    }

    private final Map<Long, Entry> modelCache = new ConcurrentHashMap<>();

    public EmbeddingModel get(EmbeddingLLM config) {
        EmbeddingCore core = EmbeddingCore.get(config.getProvider());
        if (null == core) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("Unknown model provider: ") + config.getProvider());
        }
        // Rebuild whenever the connection settings change: eviction alone misses
        // updates that bypass the admin UI (SQL, scripts), leaving a stale client
        // that keeps failing with the old credentials
        String fingerprint = String.join("|",
                Objects.toString(config.getProvider(), ""),
                Objects.toString(config.getModel(), ""),
                Objects.toString(config.getApiUrl(), ""),
                Objects.toString(config.getApiKey(), ""));
        return modelCache.compute(config.getId(), (id, cached) ->
                null != cached && cached.fingerprint().equals(fingerprint)
                        ? cached : new Entry(fingerprint, core.build(config))
        ).model();
    }

    public void evict(Long embeddingLlmId) {
        modelCache.remove(embeddingLlmId);
    }

}
