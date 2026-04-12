package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.OpenAI;

/**
 * @author YuePeng
 * date 2025/2/26 22:40
 */
@Component
public class GLM extends OpenAI {

    @Override
    public String model() {
        return "glm-4-air";
    }

    @Override
    public String api() {
        return "https://open.bigmodel.cn";
    }

    @Override
    public String chatApiPoint() {
        return "/api/paas/v4";
    }

}
