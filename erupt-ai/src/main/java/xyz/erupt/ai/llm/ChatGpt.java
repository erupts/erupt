package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.base.LlmConfig;

/**
 * @author YuePeng
 * date 2025/2/22 16:37
 */
@Component
public class ChatGpt extends OpenAiSpec {

    @Override
    public String code() {
        return "ChatGpt";
    }

    @Override
    public String model() {
        return "gpt-4";
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
