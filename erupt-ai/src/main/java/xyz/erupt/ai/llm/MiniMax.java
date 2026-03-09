package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.OpenAi;

@Component
public class MiniMax extends OpenAi {

    @Override
    public String model() {
        return "MiniMax-M2.5";
    }

    @Override
    public String api() {
        return "https://api.minmax.io";
    }

}