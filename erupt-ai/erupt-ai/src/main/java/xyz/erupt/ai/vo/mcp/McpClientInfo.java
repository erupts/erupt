package xyz.erupt.ai.vo.mcp;

import dev.langchain4j.mcp.client.McpClient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class McpClientInfo {

    private McpClient mcpClient;

    private String name;

    private String error;
}
