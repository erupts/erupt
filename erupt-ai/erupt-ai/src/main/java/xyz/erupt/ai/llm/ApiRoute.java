package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.OpenAI;

@Component
public class ApiRoute extends OpenAI {

    @Override
    public String model() {
        return "gpt-4o-mini";
    }

    @Override
    public String api() {
        return "https://global.api-route.com";
    }
}
