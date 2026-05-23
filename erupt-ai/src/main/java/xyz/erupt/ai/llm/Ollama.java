package xyz.erupt.ai.llm;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.core.SseListener;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author YuePeng
 * date 2025/2/26 22:58
 */
@Component
public class Ollama extends LlmCore {

    @Override
    public String code() {
        return Ollama.class.getSimpleName();
    }

    @Override
    public String model() {
        return "nomic-embed-text";
    }

    @Override
    public String api() {
        return "http://localhost:11434";
    }

    @Override
    public ChatModel buildChatModel(LlmRequest llmRequest, List<ChatMessage> chatMessages) {
        return OllamaChatModel.builder()
                .baseUrl(llmRequest.getUrl())
                .modelName(llmRequest.getModel())
                .topP(llmRequest.getTop_p())
                .temperature(llmRequest.getTemperature())
                .build();
    }

    @Override
    public StreamingChatModel buildStreamingChatModel(LlmRequest llmRequest, List<ChatMessage> chatMessages, Consumer<SseListener> listener) {
        return OllamaStreamingChatModel.builder()
                .baseUrl(llmRequest.getUrl())
                .modelName(llmRequest.getModel())
                .topP(llmRequest.getTop_p())
                .temperature(llmRequest.getTemperature())
                .build();
    }

}
