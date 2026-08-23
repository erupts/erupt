package xyz.erupt.ai.embedding;

import org.springframework.stereotype.Component;

/**
 * SiliconFlow hosted open-source embedding models (BGE, GTE, Qwen3-Embedding …).
 *
 * @author YuePeng
 * date 2026/8/18
 */
@Component
public class SiliconFlowEmbedding extends OpenAICompatibleEmbedding {

    @Override
    public String code() {
        return "SiliconFlow";
    }

    @Override
    public String model() {
        return "BAAI/bge-m3";
    }

    @Override
    public String api() {
        return "https://api.siliconflow.cn/v1";
    }

    @Override
    public Integer dimension() {
        return 1024;
    }

}
