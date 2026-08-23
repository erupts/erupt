package xyz.erupt.ai.service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import xyz.erupt.ai.core.EmbeddingCore;
import xyz.erupt.ai.model.EmbeddingLLM;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;

import java.util.Map;
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

    private final Map<Long, EmbeddingModel> modelCache = new ConcurrentHashMap<>();

    public EmbeddingModel get(EmbeddingLLM config) {
        EmbeddingCore core = EmbeddingCore.get(config.getProvider());
        if (null == core) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("Unknown model provider: ") + config.getProvider());
        }
        return modelCache.computeIfAbsent(config.getId(), id -> core.build(config));
    }

    public void evict(Long embeddingLlmId) {
        modelCache.remove(embeddingLlmId);
    }

}
