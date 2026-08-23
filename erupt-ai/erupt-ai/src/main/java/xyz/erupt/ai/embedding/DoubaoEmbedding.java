package xyz.erupt.ai.embedding;

import org.springframework.stereotype.Component;

/**
 * ByteDance Volcengine Ark embedding via its OpenAI-compatible endpoint.
 *
 * @author YuePeng
 * date 2026/8/18
 */
@Component
public class DoubaoEmbedding extends OpenAICompatibleEmbedding {

    @Override
    public String code() {
        return "Doubao";
    }

    @Override
    public String model() {
        return "doubao-embedding-text-240715";
    }

    @Override
    public String api() {
        return "https://ark.cn-beijing.volces.com/api/v3";
    }

    @Override
    public Integer dimension() {
        return 2560;
    }

}
