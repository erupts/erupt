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
        return new BaseLLMConfig("https://api.moonshot.cn", "moonshot-v1-8k", "sk-j7TPlrYn69CEd3VLl7u4ceLwyox7QuPP4VzpdPDwv1uzayJl");
    }

}
