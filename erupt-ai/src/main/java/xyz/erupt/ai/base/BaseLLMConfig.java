package xyz.erupt.ai.base;

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
public class BaseLLMConfig {

    private String url;

    private String apiKey;

    private String model;

    public BaseLLMConfig(String url, String model, String apiKey) {
        this.url = url;
        this.model = model;
        this.apiKey = apiKey;
    }

}
