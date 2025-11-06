package xyz.erupt.ai.vo.mcp;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class McpRequest {

    private String jsonrpc = "2.0";

    private Object id;          // String or Integer

    private String method;

    private RequestParams params;


    @Getter
    @Setter
    public static class RequestParams {

        private String name;

        private Map<String, Object> arguments;
    }

}
