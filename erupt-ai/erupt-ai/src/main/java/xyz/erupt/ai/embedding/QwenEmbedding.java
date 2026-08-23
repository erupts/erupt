package xyz.erupt.ai.embedding;

import org.springframework.stereotype.Component;

/**
 * Alibaba DashScope embedding via its OpenAI-compatible endpoint.
 *
 * @author YuePeng
 * date 2026/8/18
 */
@Component
public class QwenEmbedding extends OpenAICompatibleEmbedding {

    @Override
    public String code() {
        return "Qwen";
    }

    @Override
    public String model() {
        return "text-embedding-v4";
    }

    @Override
    public String api() {
        return "https://dashscope.aliyuncs.com/compatible-mode/v1";
    }

    @Override
    public Integer dimension() {
        return 1024;
    }

}
