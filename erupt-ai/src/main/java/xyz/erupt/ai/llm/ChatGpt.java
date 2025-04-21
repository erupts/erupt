package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.OpenAi;

/**
 * @author YuePeng
 * date 2025/2/22 16:37
 */
@Component
public class ChatGpt extends OpenAi {

    @Override
    public String code() {
        return "ChatGpt";
    }

    @Override
    public String model() {
        return "gpt-4o";
    }

    @Override
    public String api() {
        return "https://api.openai.com";
    }

    @Override
    public LlmConfig config() {
        return new LlmConfig();
    }

}
