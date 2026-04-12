package xyz.erupt.ai.llm;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.core.SseListener;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author YuePeng
 * date 2025/2/22 16:37
 */
@Component
@Slf4j
public class Gemini extends LlmCore {

    @Override
    public String model() {
        return "gemini-2.0-flash";
    }

    @Override
    public String api() {
        return "https://generativelanguage.googleapis.com";
    }

    @Override
    public String code() {
        return getClass().getSimpleName();
    }

    @Override
    public ChatModel buildChatModel(LlmRequest llmRequest, List<ChatMessage> chatMessages) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(llmRequest.getApiKey())
                .modelName(llmRequest.getModel())
                .topP(llmRequest.getTop_p())
                .temperature(llmRequest.getTemperature())
                .build();
    }

    @Override
    @SneakyThrows
    public StreamingChatModel buildStreamingChatModel(LlmRequest llmRequest, List<ChatMessage> chatMessages, Consumer<SseListener> listener) {
        return GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(llmRequest.getApiKey())
                .modelName(llmRequest.getModel())
                .topP(llmRequest.getTop_p())
                .temperature(llmRequest.getTemperature())
                .build();
    }

}
