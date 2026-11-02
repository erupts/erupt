package xyz.erupt.ai.vo.mcp;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class McpTool {

    private String name;

    public String description;

    // JSON Schema of the arguments, as langchain4j builds it for the tool
    private Map<String, Object> inputSchema;

}
