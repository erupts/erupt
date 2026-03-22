package xyz.erupt.ai.core;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.tool.AiToolboxManager;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.util.EruptSpringUtil;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public abstract class LlmCore {

    private static final Map<String, LlmCore> llms = new HashMap<>();

    public LlmCore() {
        llms.put(this.code(), this);
    }

    public static LlmCore getLLM(String code) {
        return llms.get(code);
    }

    public static LlmCore getLLM(LLM llm) {
        return llms.get(llm.getLlm());
    }

    public abstract String code();

    public abstract String model();

    public abstract String api();

    public LlmConfig config() {
        return new LlmConfig();
    }

    public abstract ChatModel buildChatModel(LlmRequest llmRequest, List<ChatMessage> chatMessages);

    public abstract StreamingChatModel buildStreamingChatModel(LlmRequest llmRequest, List<ChatMessage> chatMessages, Consumer<SseListener> listener);

    public String chat(LlmRequest llmRequest, List<ChatMessage> chatMessages) {
        ChatModel chatModel = this.buildChatModel(llmRequest, chatMessages);
        chatMessages.add(0, SystemMessage.from(EruptSpringUtil.getBean(AiProp.class).getSystemPrompt()));
        return chatModel.chat(chatMessages).aiMessage().text();
    }

    public void chatSse(LlmRequest llmRequest, List<ChatMessage> chatMessages, Consumer<SseListener> listener) {
        StreamingChatModel streamingChatModel = this.buildStreamingChatModel(llmRequest, chatMessages, listener);
        chatMessages.add(0, SystemMessage.from(EruptSpringUtil.getBean(AiProp.class).getSystemPrompt()));
        List<ToolSpecification> specs = new ArrayList<>();
        if (null != llmRequest.getAutoCallTool() && llmRequest.getAutoCallTool()) {
            for (Method method : AiToolboxManager.getAiMethodMap().values()) {
                specs.add(ToolSpecifications.toolSpecificationFrom(method));
            }
        }
        ChatRequest request = ChatRequest.builder().messages(chatMessages).toolSpecifications(specs).build();
        MetaContext metaContext = MetaContext.get();
        streamingChatModel.chat(request, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                MetaContext.set(metaContext);
                listener.accept(SseListener.builder().currMessage(partialResponse).build());
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                MetaContext.set(metaContext);
                listener.accept(SseListener.builder()
                        .isFinish(true)
                        .usage(chatResponse.tokenUsage())
                        .aiMessage(chatResponse.aiMessage()).build());
            }

            @Override
            public void onError(Throwable e) {
                log.error("Failed to get response from server", e);
                listener.accept(SseListener.builder().throwable(e).build());
            }
        });
    }

    public static class H implements ChoiceFetchHandler {
        @Override
        public List<VLModel> fetch(String[] params) {
            return llms.keySet().stream().map(it -> new VLModel(it, it))
                    .sorted(Comparator.comparing(VLModel::getLabel)).collect(Collectors.toList());
        }
    }

}
