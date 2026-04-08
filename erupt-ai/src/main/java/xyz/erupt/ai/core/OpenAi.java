package xyz.erupt.ai.core;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.llm.ChatGpt;

import java.util.List;
import java.util.Map;
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
                .sendThinking(isThinkingModel(llmRequest.getModel()))
                .returnThinking(false)
                .strictTools(this instanceof ChatGpt)
                .defaultRequestParameters(OpenAiChatRequestParameters.builder()
                        .customParameters(buildCustomParams(llmRequest.getModel()))
                        .build()
                )
                .build();
    }

    @Override
    public StreamingChatModel buildStreamingChatModel(LlmRequest llmRequest, List<ChatMessage> chatMessages, Consumer<SseListener> listener) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(llmRequest.getUrl() + chatApiPoint())
                .apiKey(llmRequest.getApiKey())
                .modelName(llmRequest.getModel())
                .topP(llmRequest.getTop_p())
                .temperature(llmRequest.getTemperature())
                .sendThinking(isThinkingModel(llmRequest.getModel()))
                .returnThinking(false)
                .strictTools(this instanceof ChatGpt)
                .defaultRequestParameters(OpenAiChatRequestParameters.builder()
                        .customParameters(buildCustomParams(llmRequest.getModel()))
                        .build()
                )
                .build();
    }

    private boolean isThinkingModel(String model) {
        return model != null && (
                model.contains("deepseek-r") ||
                        model.contains("deepseek-reasoner") ||
                        model.contains("k2-thinking")
        );
    }

    private Map<String, Object> buildCustomParams(String model) {
        if (model.contains("k2.5")) {
            return Map.of("thinking", Map.of("type", "disabled"));
        } else if (model.contains("qwen3")) {
            return Map.of("enable_thinking", false);
        } else if (model.contains("grok-3-mini")) {
            return Map.of("reasoning_effort", "none");
        }
        return Map.of();
    }

}
