package xyz.erupt.ai.vo.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class McpRequest {

    private String jsonrpc = "2.0";

    private Object id;          // String 或 Integer

    private String method;

    private RequestParams params;


    @Getter
    @Setter
    public static class RequestParams {

        private String name;

        private Map<String, Object> arguments;
    }

}
