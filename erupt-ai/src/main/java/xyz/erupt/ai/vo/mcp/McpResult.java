package xyz.erupt.ai.vo.mcp;

/**
 * @author YuePeng
 * date 2025/9/3 23:11
 */

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
