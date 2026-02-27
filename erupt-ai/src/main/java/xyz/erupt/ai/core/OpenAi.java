package xyz.erupt.ai.core;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.pojo.ChatCompletionMessage;
import xyz.erupt.core.context.MetaContext;

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
    public LlmConfig config() {
        return new LlmConfig();
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
        assistantPrompt.removeIf(it -> StringUtils.isBlank(it.getContent()));
        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl(llmRequest.getUrl() + chatApiPoint())
                .apiKey(llmRequest.getApiKey())
                .modelName(llmRequest.getModel())
                .topP(llmRequest.getTop_p())
                .temperature(llmRequest.getTemperature())
                .build();
        MetaContext metaContext = MetaContext.get();
        model.chat(userMessage, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                MetaContext.set(metaContext);
                SseListener sseListener = new SseListener();
                sseListener.setCurrData(partialResponse);
                sseListener.getOutput().append(partialResponse);
                sseListener.setCurrMessage(partialResponse);
                listener.accept(sseListener);
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                SseListener sseListener = new SseListener();
                sseListener.setFinish(true);
                sseListener.setUsage(chatResponse.tokenUsage());
                listener.accept(sseListener);
            }

            @Override
            public void onError(Throwable e) {
                log.error("Failed to get response from server", e);
                SseListener sseListener = new SseListener();
                sseListener.setError(true);
                sseListener.setFinish(true);
                sseListener.getOutput().append(e.getMessage());
                sseListener.setCurrMessage(e.getMessage());
                listener.accept(sseListener);
            }
        });
    }

}
