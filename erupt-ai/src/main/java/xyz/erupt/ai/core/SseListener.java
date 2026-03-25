package xyz.erupt.ai.core;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.output.TokenUsage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2025/2/23 14:34
 */
@Getter
@Setter
@Builder
public class SseListener {

    // Stream output, the content of the current message
    private String currMessage;

    private String think;

    private boolean isFinish;

    // Spending tokens
    private TokenUsage usage;

    private AiMessage aiMessage;

    private StreamingChatModel streamingChatModel;

    private Throwable throwable;
}
