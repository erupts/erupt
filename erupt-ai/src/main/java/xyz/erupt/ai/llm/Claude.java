package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.OpenAi;

/**
 * @author YuePeng
 * date 2025/2/22 16:37
 */
@Component
public class Claude extends OpenAi {

    @Override
    public String code() {
        return "Claude";
    }

    @Override
    public String model() {
        return "claude-3-7-sonnet-latest";
    }

    @Override
    public String api() {
        return "https://api.anthropic.com";
    }

    @Override
    public LlmConfig config() {
        return new LlmConfig();
    }

}
