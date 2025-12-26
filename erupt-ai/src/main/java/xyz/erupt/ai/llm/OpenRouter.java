package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.OpenAi;

@Component
public class OpenRouter extends OpenAi {

    @Override
    public String model() {
        return "openai/gpt-4o";
    }

    @Override
    public String api() {
        return "https://openrouter.ai";
    }

}