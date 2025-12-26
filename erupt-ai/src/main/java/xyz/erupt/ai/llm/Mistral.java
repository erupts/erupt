package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.OpenAi;

@Component
public class Mistral extends OpenAi {

    @Override
    public String model() {
        return "mistral-large-latest";
    }

    @Override
    public String api() {
        return "https://api.mistral.ai";
    }

}