package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmConfig;
import xyz.erupt.ai.core.OpenAi;

/**
 * @author YuePeng
 * date 2025/2/26 22:58
 */
@Component
public class Ollama extends OpenAi {

    @Override
    public String code() {
        return "Ollama";
    }

    @Override
    public String model() {
        return "nomic-embed-text";
    }

    @Override
    public String api() {
        return "http://localhost:11434";
    }

    @Override
    public LlmConfig config() {
        return new LlmConfig();
    }

}
