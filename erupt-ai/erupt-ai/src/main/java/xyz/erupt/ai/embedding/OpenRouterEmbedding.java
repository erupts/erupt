package xyz.erupt.ai.embedding;

import org.springframework.stereotype.Component;

/**
 * OpenRouter embedding via its OpenAI-compatible endpoint.
 * Model names are provider-prefixed, e.g. "openai/text-embedding-3-small".
 *
 * @author YuePeng
 * date 2026/8/18
 */
@Component
public class OpenRouterEmbedding extends OpenAICompatibleEmbedding {

    @Override
    public String code() {
        return "OpenRouter";
    }

    @Override
    public String model() {
        return "openai/text-embedding-3-small";
    }

    @Override
    public String api() {
        return "https://openrouter.ai/api/v1";
    }

    @Override
    public Integer dimension() {
        return 1536;
    }

}
