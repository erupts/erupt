package xyz.erupt.ai.core;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.constants.ResponseFormat;

/**
 * @author YuePeng
 * date 2025/4/15 22:28
 */
@Getter
@Setter
public class LlmRequest {

    private String url;

    private String apiKey;

    private String model;

    private Float temperature;

    private Float top_p;

    private ResponseFormat responseFormat = ResponseFormat.text;

}
