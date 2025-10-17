package xyz.erupt.ai.core;

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

    private boolean pending = false;

    // Stream output, the content of the current message
    private String currMessage;

    // Stream output, the entire message object
    private String currData;

    // Spending tokens
    private ChatUsage usage = new ChatUsage();

    private boolean isFinish = false;

    private boolean error = false;
}
