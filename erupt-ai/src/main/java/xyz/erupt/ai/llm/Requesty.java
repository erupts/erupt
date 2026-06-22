package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.OpenAI;

@Component
public class Requesty extends OpenAI {

    @Override
    public String model() {
        return "openai/gpt-4o-mini";
    }

    @Override
    public String api() {
        return "https://router.requesty.ai";
    }
}
