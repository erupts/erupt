package xyz.erupt.ai.embedding;

import org.springframework.stereotype.Component;

/**
 * Cohere embedding via its OpenAI compatibility API
 * (https://docs.cohere.com/docs/compatibility-api).
 *
 * @author YuePeng
 * date 2026/8/18
 */
@Component
public class CohereEmbedding extends OpenAICompatibleEmbedding {

    @Override
    public String code() {
        return "Cohere";
    }

    @Override
    public String model() {
        return "embed-v4.0";
    }

    @Override
    public String api() {
        return "https://api.cohere.ai/compatibility/v1";
    }

    @Override
    public Integer dimension() {
        return 1536;
    }

}
