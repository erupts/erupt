package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.base.BaseLLMConfig;

/**
 * @author YuePeng
 * date 2025/2/23 15:31
 */
@Component
public class Moonshot extends CommonLLM {

    @Override
    public String code() {
        return "Moonshot";
    }

    @Override
    public String model() {
        return "moonshot-v1-8k";
    }

    @Override
    public BaseLLMConfig config() {
        return new BaseLLMConfig("https://api.moonshot.cn", "");
    }

}
