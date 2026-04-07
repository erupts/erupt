package xyz.erupt.ai.core;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author YuePeng
 * date 2025/2/25 22:07
 */
@Component
@Slf4j
public abstract class OpenAi extends LlmCore {

    public String chatApiPoint() {
        return "/v1";
    }

    @Override
    public String code() {
        return getClass().getSimpleName();
    }

    @Override
    public ChatModel buildChatModel(LlmRequest llmRequest, List<ChatMessage> chatMessages) {
        return OpenAiChatModel.builder()
                .baseUrl(llmRequest.getUrl() + chatApiPoint())
                .apiKey(llmRequest.getApiKey())
                .modelName(llmRequest.getModel())
                .topP(llmRequest.getTop_p())
                .temperature(llmRequest.getTemperature())
                .strictTools(llmRequest.getStrictTools())
                .build();
    }

    @Override
    @SneakyThrows
    public StreamingChatModel buildStreamingChatModel(LlmRequest llmRequest, List<ChatMessage> chatMessages, Consumer<SseListener> listener) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(llmRequest.getUrl() + chatApiPoint())
                .apiKey(llmRequest.getApiKey())
                .modelName(llmRequest.getModel())
                .topP(llmRequest.getTop_p())
                .temperature(llmRequest.getTemperature())
                .strictTools(llmRequest.getStrictTools())
                .build();
    }

}
