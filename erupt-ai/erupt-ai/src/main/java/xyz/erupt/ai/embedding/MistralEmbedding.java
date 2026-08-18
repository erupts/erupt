package xyz.erupt.ai.embedding;

import org.springframework.stereotype.Component;

/**
 * Mistral embedding via its OpenAI-compatible endpoint.
 *
 * @author YuePeng
 * date 2026/8/18
 */
@Component
public class MistralEmbedding extends OpenAICompatibleEmbedding {

    @Override
    public String code() {
        return "Mistral";
    }

    @Override
    public String model() {
        return "mistral-embed";
    }

    @Override
    public String api() {
        return "https://api.mistral.ai/v1";
    }

    @Override
    public Integer dimension() {
        return 1024;
    }

}
