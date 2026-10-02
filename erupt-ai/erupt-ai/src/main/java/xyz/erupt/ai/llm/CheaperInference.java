package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.OpenAI;

@Component
public class CheaperInference extends OpenAI {

    @Override
    public String model() {
        return "gpt-5.4-mini";
    }

    @Override
    public String api() {
        return "https://api.cheaperinference.com";
    }
}
