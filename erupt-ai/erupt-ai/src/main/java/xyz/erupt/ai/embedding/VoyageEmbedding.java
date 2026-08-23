package xyz.erupt.ai.embedding;

import org.springframework.stereotype.Component;

/**
 * Voyage AI embedding (the provider Anthropic recommends) via its
 * OpenAI-compatible endpoint.
 *
 * @author YuePeng
 * date 2026/8/18
 */
@Component
public class VoyageEmbedding extends OpenAICompatibleEmbedding {

    @Override
    public String code() {
        return "Voyage AI";
    }

    @Override
    public String model() {
        return "voyage-3.5";
    }

    @Override
    public String api() {
        return "https://api.voyageai.com/v1";
    }

    @Override
    public Integer dimension() {
        return 1024;
    }

}
