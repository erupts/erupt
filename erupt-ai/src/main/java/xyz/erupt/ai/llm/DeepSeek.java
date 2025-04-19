package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.base.LlmConfig;

/**
 * @author YuePeng
 * date 2025/2/22 16:37
 */
@Component
public class DeepSeek extends OpenAiSpec {

    @Override
    public String code() {
        return "DeepSeek";
    }

    @Override
    public String model() {
        return "deepseek-chat";
    }

    @Override
    public String api() {
        return "https://api.deepseek.com";
    }

    @Override
    public LlmConfig config() {
        return new LlmConfig();
    }

}
