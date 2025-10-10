package xyz.erupt.ai.vo.mcp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author YuePeng
 * date 2025/9/4 20:31
 */
@Getter
@Setter
public class McpCallResult {

    private List<Content> content;

    private boolean isError = false;

    @Getter
    @Setter
    public static class Content {
        private String text;
        private String type = "text";
    }

}
