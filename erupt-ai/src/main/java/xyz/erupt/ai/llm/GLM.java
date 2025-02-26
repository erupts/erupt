package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.base.BaseLLMConfig;

/**
 * @author YuePeng
 * date 2025/2/26 22:40
 */
@Component
public class GLM extends CommonLLM {

    @Override
    public String code() {
        return "ChatGLM4(智普)";
    }

    @Override
    public String model() {
        return "glm-4-air";
    }

    @Override
    public String chatApiPath() {
        return "/api/paas/v4/chat/completions";
    }

    @Override
    public BaseLLMConfig config() {
        return new BaseLLMConfig("https://open.bigmodel.cn", "glm-4-air", "");
    }

}
