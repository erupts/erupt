package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.OpenAi;

@Component
public class Grok extends OpenAi {

    @Override
    public String model() {
        return "grok-2-latest";
    }

    @Override
    public String api() {
        return "https://api.x.ai";
    }

}