package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.core.OpenAI;

/**
 * @author YuePeng
 * date 2025/2/22 16:37
 */
@Component
public class Mimo extends OpenAI {

    @Override
    public String model() {
        return "mimo-v2.5-pro";
    }

    @Override
    public String api() {
        return "https://token-plan-cn.xiaomimimo.com";
    }

    @Override
    protected boolean returnThinking(LlmRequest llmRequest) {
        return true;
    }

    @Override
    protected boolean sendThinking(LlmRequest llmRequest) {
        return true;
    }

}
