package xyz.erupt.ai.core;

import dev.langchain4j.model.output.TokenUsage;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.ai.pojo.ChatUsage;

/**
 * @author YuePeng
 * date 2025/2/23 14:34
 */
@Getter
@Setter
public class SseListener {

    // Full output content
    private final StringBuilder output = new StringBuilder();

    // Stream output, the content of the current message
    private String currMessage;

    // Stream output, the entire message object
    private String currData;

    // Spending tokens
    private TokenUsage usage;

    private boolean isFinish = false;

    private boolean error = false;
}
