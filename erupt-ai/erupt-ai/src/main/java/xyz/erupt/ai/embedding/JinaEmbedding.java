package xyz.erupt.ai.embedding;

import org.springframework.stereotype.Component;

/**
 * Jina AI embedding via its OpenAI-compatible endpoint.
 *
 * @author YuePeng
 * date 2026/8/18
 */
@Component
public class JinaEmbedding extends OpenAICompatibleEmbedding {

    @Override
    public String code() {
        return "Jina";
    }

    @Override
    public String model() {
        return "jina-embeddings-v3";
    }

    @Override
    public String api() {
        return "https://api.jina.ai/v1";
    }

    @Override
    public Integer dimension() {
        return 1024;
    }

}
