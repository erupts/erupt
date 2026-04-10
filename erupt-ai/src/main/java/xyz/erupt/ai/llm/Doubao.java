package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.OpenAI;

@Component
public class Doubao extends OpenAI {

    @Override
    public String model() {
        return "doubao-pro-32k";
    }

    @Override
    public String api() {
        return "https://ark.cn-beijing.volces.com";
    }

    @Override
    public String chatApiPoint() {
        return "/api/v3";
    }

}
