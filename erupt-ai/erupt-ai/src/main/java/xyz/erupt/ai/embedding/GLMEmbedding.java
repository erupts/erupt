package xyz.erupt.ai.embedding;

import org.springframework.stereotype.Component;

/**
 * Zhipu GLM embedding via its OpenAI-compatible endpoint.
 *
 * @author YuePeng
 * date 2026/8/18
 */
@Component
public class GLMEmbedding extends OpenAICompatibleEmbedding {

    @Override
    public String code() {
        return "GLM";
    }

    @Override
    public String model() {
        return "embedding-3";
    }

    @Override
    public String api() {
        return "https://open.bigmodel.cn/api/paas/v4";
    }

    @Override
    public Integer dimension() {
        return 2048;
    }

}
