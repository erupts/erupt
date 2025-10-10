package xyz.erupt.ai.vo.mcp;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class McpInfo {

    private String protocolVersion = "2024-11-05";

    private Map<String, Object> capabilities;

    private ServerInfo serverInfo;

    @Getter
    @Setter
    public static class ServerInfo {

        private String name;

        private String version;

    }

}
