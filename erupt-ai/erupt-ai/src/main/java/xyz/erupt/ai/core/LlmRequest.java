package xyz.erupt.ai.core;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.constants.ResponseFormat;

import java.util.List;

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

    private Double temperature;

    private Double top_p;

    private Boolean autoCallTool = false;

    private Boolean strictTools = true;

    private boolean thinking = false;

    private ResponseFormat responseFormat = ResponseFormat.text;

    private String agentPrompt;

    private String contextPrompt;

    // Request-scoped tool objects (langchain4j @Tool methods) driving a ReAct
    // loop for this call only; independent from autoCallTool, which exposes the
    // global toolbox and MCP tools instead
    private List<Object> tools;

}
