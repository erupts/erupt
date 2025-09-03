package xyz.erupt.ai.vo.mcp;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class McpTool {

    private String name;

    public String description;

    private InputSchema inputSchema;

    @Getter
    @Setter
    public static class InputSchema {

        private String type = "object";

        private Map<String, SchemaProperties> properties = new HashMap<>();

        private List<String> required = new ArrayList<>();

    }

    @Getter
    @Setter
    public static class SchemaProperties {

        private String type;

        private String description;

    }

}
