package xyz.erupt.ai.embedding;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.EmbeddingCore;
import xyz.erupt.ai.model.EmbeddingLLM;

/**
 * OpenAI-compatible embedding endpoint (/v1/embeddings) — covers OpenAI, Qwen,
 * GLM, Doubao, SiliconFlow, local BGE servers and most other providers.
 * The `dimensions` request parameter is intentionally not sent for maximum
 * compatibility; the configured dimension is validated by the test button instead.
 *
 * @author YuePeng
 * date 2026/8/17
 */
@Component
public class OpenAICompatibleEmbedding extends EmbeddingCore {

    @Override
    public String code() {
        return "OpenAI Compatible";
    }

    @Override
    public String model() {
        return "text-embedding-3-small";
    }

    @Override
    public String api() {
        return "https://api.openai.com/v1";
    }

    @Override
    public Integer dimension() {
        return 1536;
    }

    @Override
    public EmbeddingModel build(EmbeddingLLM config) {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(config.getApiUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModel())
                .build();
    }

}
