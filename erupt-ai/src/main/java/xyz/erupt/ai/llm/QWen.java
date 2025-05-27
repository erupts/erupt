package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.OpenAi;

/**
 * @author YuePeng
 * date 2025/2/26 22:58
 */
@Component
public class QWen extends OpenAi {

    @Override
    public String chatApiPath() {
        return "/compatible-mode/v1/chat/completions";
    }

    @Override
    public String code() {
        return "Qwen";
    }

    @Override
    public String model() {
        return "qwen-plus";
    }

    @Override
    public String api() {
        return "https://dashscope.aliyuncs.com";
    }

    @Override
    public LlmConfig config() {
        return new LlmConfig();
    }

}
