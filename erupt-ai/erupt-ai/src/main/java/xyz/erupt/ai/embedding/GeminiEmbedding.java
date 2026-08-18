package xyz.erupt.ai.embedding;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.EmbeddingCore;
import xyz.erupt.ai.model.EmbeddingLLM;

/**
 * @author YuePeng
 * date 2026/8/17
 */
@Component
public class GeminiEmbedding extends EmbeddingCore {

    @Override
    public String code() {
        return "Gemini";
    }

    @Override
    public String model() {
        return "gemini-embedding-001";
    }

    @Override
    public String api() {
        return "https://generativelanguage.googleapis.com/v1beta";
    }

    @Override
    public Integer dimension() {
        return 3072;
    }

    @Override
    public EmbeddingModel build(EmbeddingLLM config) {
        return GoogleAiEmbeddingModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModel())
                .outputDimensionality(config.getDimension())
                .build();
    }

}
