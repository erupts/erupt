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

    private Float top_p;

    private Float temperature;

    public LlmRequest toLlmRequest() {
        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setTop_p(this.top_p);
        llmRequest.setTemperature(this.temperature);
        return llmRequest;
    }

}
