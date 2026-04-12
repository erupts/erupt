package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.OpenAI;

@Component
public class Mistral extends OpenAI {

    @Override
    public String model() {
        return "mistral-large-latest";
    }

    @Override
    public String api() {
        return "https://api.mistral.ai";
    }

}