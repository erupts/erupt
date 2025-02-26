package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.base.BaseLLMConfig;

/**
 * @author YuePeng
 * date 2025/2/22 16:37
 */
@Component
public class ChatGpt extends CommonLLM {

    @Override
    public String code() {
        return "ChatGpt";
    }

    @Override
    public String model() {
        return "gpt-4";
    }

    @Override
    public BaseLLMConfig config() {
        return new BaseLLMConfig("https://api.openai.com", "gpt-4", "");
    }

}
