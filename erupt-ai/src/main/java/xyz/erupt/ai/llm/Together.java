package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.OpenAI;

@Component
public class Together extends OpenAI {

    @Override
    public String model() {
        return "meta-llama/Meta-Llama-3.1-70B-Instruct";
    }

    @Override
    public String api() {
        return "https://api.together.xyz";
    }

}