package xyz.erupt.ai.core;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.pojo.ChatCompletionMessage;

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
    public String chat(LlmRequest llmRequest, String userMessage, List<ChatCompletionMessage> assistantPrompt) {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(llmRequest.getUrl() + chatApiPoint())
                .apiKey(llmRequest.getApiKey())
                .modelName(llmRequest.getModel())
                .topP(llmRequest.getTop_p())
                .temperature(llmRequest.getTemperature())
                .build();
        return model.chat(userMessage);
    }

    @Override
    @SneakyThrows
    public void chatSse(LlmRequest llmRequest, String userMessage, List<ChatCompletionMessage> assistantPrompt, Consumer<SseListener> listener) {
        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl(llmRequest.getUrl() + chatApiPoint())
                .apiKey(llmRequest.getApiKey())
                .modelName(llmRequest.getModel())
                .topP(llmRequest.getTop_p())
                .temperature(llmRequest.getTemperature())
                .build();
        this.streamerReact(listener, model, userMessage, assistantPrompt);
    }

}
