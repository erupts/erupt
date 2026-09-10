package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.OpenAI;

@Component
public class OpenRouter extends OpenAI {

    @Override
    public String model() {
        return "deepseek/deepseek-r1";
    }

    @Override
    public String api() {
        return "https://openrouter.ai";
    }

    @Override
    public String chatApiPoint() {
        return "/api/v1";
    }
}