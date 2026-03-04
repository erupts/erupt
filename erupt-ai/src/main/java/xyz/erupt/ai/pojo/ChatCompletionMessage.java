package xyz.erupt.ai.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.erupt.ai.constants.MessageRole;

/**
 * @author YuePeng
 * date 2025/2/25 21:52
 */
@Getter
@Setter
@NoArgsConstructor
public class ChatCompletionMessage {

    private MessageRole role;

    private String content;

    private String toolName;

    private String toolId;

    public ChatCompletionMessage(MessageRole role, String content) {
        this.role = role;
        this.content = content;
    }

    public ChatCompletionMessage(MessageRole role, String content, String toolName, String toolId) {
        this.role = role;
        this.content = content;
        this.toolName = toolName;
        this.toolId = toolId;
    }
}
