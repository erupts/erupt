package xyz.erupt.ai.embedding;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.EmbeddingCore;
import xyz.erupt.ai.model.EmbeddingLLM;

/**
 * @author YuePeng
 * date 2026/8/17
 */
@Component
public class OllamaEmbedding extends EmbeddingCore {

    @Override
    public String code() {
        return "Ollama";
    }

    @Override
    public String model() {
        return "nomic-embed-text";
    }

    @Override
    public String api() {
        return "http://localhost:11434";
    }

    @Override
    public Integer dimension() {
        return 768;
    }

    @Override
    public EmbeddingModel build(EmbeddingLLM config) {
        return OllamaEmbeddingModel.builder()
                .baseUrl(config.getApiUrl())
                .modelName(config.getModel())
                .build();
    }

}
