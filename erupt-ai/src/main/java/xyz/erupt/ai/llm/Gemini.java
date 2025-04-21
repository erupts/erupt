package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.OpenAi;

/**
 * @author YuePeng
 * date 2025/2/22 16:37
 */
@Component
public class Gemini extends OpenAi {

    @Override
    public String code() {
        return "Gemini";
    }

    @Override
    public String model() {
        return "gemini-2.0-flash";
    }

    @Override
    public String api() {
        return "https://generativelanguage.googleapis.com";
    }

    @Override
    public LlmConfig config() {
        return new LlmConfig();
    }

}
