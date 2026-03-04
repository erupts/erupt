package xyz.erupt.ai.core;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import xyz.erupt.ai.tool.AiToolboxManager;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.constants.MessageRole;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.pojo.ChatCompletionMessage;
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


    public abstract String chat(LlmRequest llmRequest, String userMessage, List<ChatCompletionMessage> assistantPrompt);

    public abstract void chatSse(LlmRequest llmRequest, String userMessage, List<ChatCompletionMessage> assistantPrompt, Consumer<SseListener> listener);

    protected void streamerReact(Consumer<SseListener> listener, StreamingChatModel streamingChatModel, String userMessage, List<ChatCompletionMessage> chatCompletionMessages) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(EruptSpringUtil.getBean(AiProp.class).getSystemPrompt()));
        for (ChatCompletionMessage message : chatCompletionMessages) {
            if (message.getRole() == MessageRole.assistant) {
                messages.add(AiMessage.from(message.getContent()));
            } else if (message.getRole() == MessageRole.user) {
                messages.add(UserMessage.from(message.getContent()));
            } else if (message.getRole() == MessageRole.system) {
                messages.add(SystemMessage.from(message.getContent()));
            } else if (message.getRole() == MessageRole.tool) {
                messages.add(ToolExecutionResultMessage.from(message.getToolId(), message.getToolName(), message.getContent()));
            }
        }

        List<ToolSpecification> specs = new ArrayList<>();
        for (Method method : AiToolboxManager.getAiMethodMap().values()) {
            specs.add(ToolSpecifications.toolSpecificationFrom(method));
        }
        ChatRequest request = ChatRequest.builder().messages(messages).toolSpecifications(specs).build();
        MetaContext metaContext = MetaContext.get();
        messages.add(UserMessage.from(userMessage));
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
