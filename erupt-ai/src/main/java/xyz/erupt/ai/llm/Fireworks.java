package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.OpenAi;

@Component
public class Fireworks extends OpenAi {

    @Override
    public String model() {
        return "accounts/fireworks/models/llama-v3p1-70b-instruct";
    }

    @Override
    public String api() {
        return "https://api.fireworks.ai/inference";
    }

    @Override
    public LlmConfig config() {
        return new LlmConfig();
    }

}