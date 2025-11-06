package xyz.erupt.ai.vo.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpResult {

    private String jsonrpc = "2.0";

    private Object id;

    private Object result;

}
