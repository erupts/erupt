package xyz.erupt.test.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.core.SseListener;

import java.util.List;
import java.util.function.Consumer;

/**
 * A model that answers every request with {@link #answer} and calls no provider, registered in the one
 * application context every test shares (see the testing notes in AGENTS.md). It lets a test drive the
 * real path around a model call: the system prompt assembly, the transaction, the persistence.
 */
@Component
public class StandInLlm extends LlmCore {

    static final String CODE = "StandIn";

    static volatile String answer = "";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public String model() {
        return "stand-in";
    }

    @Override
    public String api() {
        return "http://localhost";
    }

    @Override
    public ChatModel buildChatModel(LlmRequest llmRequest, List<ChatMessage> chatMessages) {
        return new ChatModel() {
            @Override
            public ChatResponse doChat(ChatRequest chatRequest) {
                return ChatResponse.builder().aiMessage(AiMessage.from(answer)).build();
            }
        };
    }

    @Override
    public StreamingChatModel buildStreamingChatModel(LlmRequest llmRequest, List<ChatMessage> chatMessages, Consumer<SseListener> listener) {
        throw new UnsupportedOperationException();
    }

}
