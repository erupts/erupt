package xyz.erupt.ai.vo.mcp;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class McpServerSse {

    private String url = "";

    private Map<String, String> headers = new HashMap<>();
}
