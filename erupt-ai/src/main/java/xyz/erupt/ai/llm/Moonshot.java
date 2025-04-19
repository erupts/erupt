package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.base.LlmConfig;

/**
 * @author YuePeng
 * date 2025/2/23 15:31
 */
@Component
public class Moonshot extends OpenAiSpec {

    @Override
    public String code() {
        return "Moonshot";
    }

    @Override
    public String model() {
        return "moonshot-v1-8k";
    }

    @Override
    public String api() {
        return "https://api.moonshot.cn";
    }

    @Override
    public LlmConfig config() {
        return new LlmConfig();
    }

}
