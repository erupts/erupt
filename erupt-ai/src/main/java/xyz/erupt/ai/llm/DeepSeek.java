package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.OpenAi;

/**
 * @author YuePeng
 * date 2025/2/22 16:37
 */
@Component
public class DeepSeek extends OpenAi {

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
