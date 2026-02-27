//package xyz.erupt.ai.llm;
//
//import dev.langchain4j.model.anthropic.AnthropicChatModel;
//import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
//import dev.langchain4j.model.chat.response.ChatResponse;
//import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Component;
//import xyz.erupt.ai.core.LlmConfig;
//import xyz.erupt.ai.core.LlmCore;
//import xyz.erupt.ai.core.LlmRequest;
//import xyz.erupt.ai.core.SseListener;
//import xyz.erupt.ai.pojo.ChatCompletionMessage;
//import xyz.erupt.core.context.MetaContext;
//
//import java.util.List;
//import java.util.function.Consumer;
//
///**
// * @author YuePeng
// * date 2025/2/22 16:37
// */
//@Component
//@Slf4j
//public class Claude extends LlmCore {
//
//    @Override
//    public String model() {
//        return "claude-3-7-sonnet-latest";
//    }
//
//    @Override
//    public String api() {
//        return "https://api.anthropic.com";
//    }
//
//    @Override
//    public String code() {
//        return getClass().getSimpleName();
//    }
//
//    @Override
//    public LlmConfig config() {
//        return new LlmConfig();
//    }
//
//    @Override
//    public String chat(LlmRequest llmRequest, String userMessage, List<ChatCompletionMessage> assistantPrompt) {
//        AnthropicChatModel model = AnthropicChatModel.builder()
//                .baseUrl(llmRequest.getUrl())
//                .apiKey(llmRequest.getApiKey())
//                .modelName(llmRequest.getModel())
//                .topP(llmRequest.getTop_p())
//                .temperature(llmRequest.getTemperature())
//                .build();
//        return model.chat(userMessage);
//    }
//
//    @Override
//    @SneakyThrows
//    public void chatSse(LlmRequest llmRequest, String userMessage, List<ChatCompletionMessage> assistantPrompt, Consumer<SseListener> listener) {
//        assistantPrompt.removeIf(it -> StringUtils.isBlank(it.getContent()));
//        AnthropicStreamingChatModel model = AnthropicStreamingChatModel.builder()
//                .baseUrl(llmRequest.getUrl())
//                .apiKey(llmRequest.getApiKey())
//                .modelName(llmRequest.getModel())
//                .topP(llmRequest.getTop_p())
//                .temperature(llmRequest.getTemperature())
//                .build();
//        MetaContext metaContext = MetaContext.get();
//        model.chat(userMessage, new StreamingChatResponseHandler() {
//            @Override
//            public void onPartialResponse(String partialResponse) {
//                MetaContext.set(metaContext);
//                SseListener sseListener = new SseListener();
//                sseListener.setCurrData(partialResponse);
//                sseListener.getOutput().append(partialResponse);
//                sseListener.setCurrMessage(partialResponse);
//                listener.accept(sseListener);
//            }
//
//            @Override
//            public void onCompleteResponse(ChatResponse chatResponse) {
//                SseListener sseListener = new SseListener();
//                sseListener.setUsage(chatResponse.tokenUsage());
//                sseListener.setFinish(true);
//                listener.accept(sseListener);
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                log.error("Failed to get response from server", e);
//                SseListener sseListener = new SseListener();
//                sseListener.setError(true);
//                sseListener.setFinish(true);
//                sseListener.getOutput().append(e.getMessage());
//                sseListener.setCurrMessage(e.getMessage());
//                listener.accept(sseListener);
//            }
//        });
//    }
//
//}
