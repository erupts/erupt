package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.OpenAi;

@Component
public class MinMax extends OpenAi {

    @Override
    public String model() {
        return "minmax-7b-chat";
    }

    @Override
    public String api() {
        return "https://api.minmax.ai";
    }

    @Override
    public LlmConfig config() {
        return new LlmConfig();
    }

}