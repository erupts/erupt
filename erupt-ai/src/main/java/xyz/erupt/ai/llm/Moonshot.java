package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.OpenAI;

/**
 * @author YuePeng
 * date 2025/2/23 15:31
 */
@Component
public class Moonshot extends OpenAI {

    @Override
    public String model() {
        return "kimi-k2.5";
    }

    @Override
    public String api() {
        return "https://api.moonshot.cn";
    }

}
