package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.OpenAI;

@Component
public class OrcaRouter extends OpenAI {

    @Override
    public String model() {
        return "openai/gpt-5.5";
    }

    @Override
    public String api() {
        return "https://api.orcarouter.ai";
    }
}
