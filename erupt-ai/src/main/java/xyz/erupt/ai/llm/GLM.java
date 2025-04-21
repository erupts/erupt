package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.OpenAi;

/**
 * @author YuePeng
 * date 2025/2/26 22:40
 */
@Component
public class GLM extends OpenAi {

    @Override
    public String code() {
        return "ChatGLM";
    }

    @Override
    public String model() {
        return "glm-4-air";
    }

    @Override
    public String api() {
        return "https://open.bigmodel.cn";
    }

    @Override
    public String chatApiPath() {
        return "/api/paas/v4/chat/completions";
    }

    @Override
    public LlmConfig config() {
        return new LlmConfig();
    }

}
