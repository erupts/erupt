package xyz.erupt.ai.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2025/2/23 13:43
 */
@Getter
@Setter
@NoArgsConstructor
public class LlmConfig {

    private Double top_p;

    private Double temperature;

    private Boolean strictTools = true;

    // Enable thinking/reasoning mode (e.g. DeepSeek-R1, MiMo).
    // When true: reasoning_content is captured from responses and passed back in subsequent requests.
    private Boolean thinking = false;

    public LlmRequest toLlmRequest() {
        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setTop_p(this.top_p);
        llmRequest.setTemperature(this.temperature);
        llmRequest.setStrictTools(this.strictTools);
        llmRequest.setThinking(Boolean.TRUE.equals(this.thinking));
        return llmRequest;
    }

}
