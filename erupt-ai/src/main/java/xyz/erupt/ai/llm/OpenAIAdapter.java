package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.OpenAI;

/**
 * @author YuePeng
 * date 2025/2/22 16:37
 */
@Component
public class OpenAIAdapter extends OpenAI {

    @Override
    public String code() {
        return "Open AI Adapter";
    }

    @Override
    public String model() {
        return "";
    }

    @Override
    public String api() {
        return "https://";
    }

}
