package xyz.erupt.ai.core;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import xyz.erupt.ai.ask.EruptAiChat;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.tool.AiToolboxManager;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.util.EruptSpringUtil;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        String userMessage = ((dev.langchain4j.data.message.UserMessage) chatMessages.get(chatMessages.size() - 1)).singleText();
        List<ChatMessage> historyMessages = chatMessages.subList(0, chatMessages.size() - 1);
        ChatModel chatModel = this.buildChatModel(llmRequest, chatMessages);
        ChatMemory chatMemory = creatMemory(historyMessages);
        AiServices<EruptAiChat> eruptAiServices = AiServices.builder(EruptAiChat.class)
                .chatModel(chatModel).chatMemoryProvider((id) -> chatMemory);
        return this.buildAiServices(eruptAiServices, llmRequest).chat(userMessage).text();
    }

    public void chatSse(LlmRequest llmRequest, String userMessage, List<ChatMessage> chatContext, Consumer<SseListener> listener) {
        StreamingChatModel streamingChatModel = this.buildStreamingChatModel(llmRequest, chatContext, listener);
        ChatMemory chatMemory = creatMemory(chatContext);
        AiServices<EruptAiChat> eruptAiServices = AiServices.builder(EruptAiChat.class)
                .streamingChatModel(streamingChatModel).chatMemoryProvider((id) -> chatMemory);
        MetaContext metaContext = MetaContext.get();
        this.streamingChat(this.buildAiServices(eruptAiServices, llmRequest), userMessage, metaContext, listener, chatMemory);
    }

    private EruptAiChat buildAiServices(AiServices<EruptAiChat> eruptAiServices, LlmRequest llmRequest) {
        eruptAiServices.systemMessageProvider((id) -> EruptSpringUtil.getBean(AiProp.class).getSystemPrompt());
        if (llmRequest.getAutoCallTool() && !AiToolboxManager.getTools().isEmpty()) {
            eruptAiServices.tools(AiToolboxManager.getTools());
        }
        return eruptAiServices.build();
    }

    private void streamingChat(EruptAiChat eruptAiChat, String userMessage, MetaContext metaContext, Consumer<SseListener> listener, ChatMemory chatMemory) {
        MetaContext.set(metaContext);
        eruptAiChat.streamChat(userMessage).onPartialResponse(partialResponse ->
                listener.accept(SseListener.builder().currMessage(partialResponse).build())).onCompleteResponse(chatResponse -> {
            if (chatResponse.aiMessage().hasToolExecutionRequests()) {
                chatMemory.add(chatResponse.aiMessage());
                chatResponse.aiMessage().toolExecutionRequests().forEach(it -> {
                    log.info("Invoke tool: {} with arguments: {}", it.name(), it.arguments());
                    Object result = AiToolboxManager.invoke(it);
                    chatMemory.add(ToolExecutionResultMessage.from(it, null == result ? "" : result.toString()));
                });
                this.streamingChat(eruptAiChat, userMessage, metaContext, listener, chatMemory);
            } else {
                listener.accept(SseListener.builder()
                        .isFinish(true)
                        .usage(chatResponse.tokenUsage())
                        .aiMessage(chatResponse.aiMessage()).build());
            }
        }).onError(e -> {
            log.error("Failed to get response from server", e);
            listener.accept(SseListener.builder().throwable(e).build());
        }).onPartialToolCall(toolCall -> {
            MetaContext.set(metaContext);
            log.info("Calling {} with arguments: {}", toolCall.name(), toolCall.partialArguments());
            listener.accept(SseListener.builder().think("Calling " + toolCall.name()).build());
        }).onPartialThinking(thinking ->
                listener.accept(SseListener.builder().think(thinking.text()).build())
        ).start();
    }

    public ChatMemory creatMemory(List<ChatMessage> chatMessages) {
        return new ChatMemory() {

            @Override
            public Object id() {
                return null;
            }

            @Override
            public void add(ChatMessage chatMessage) {
                chatMessages.add(chatMessage);
            }

            @Override
            public List<ChatMessage> messages() {
                return chatMessages;
            }

            @Override
            public void clear() {

            }
        };
    }

    public static class H implements ChoiceFetchHandler {
        @Override
        public List<VLModel> fetch(String[] params) {
            return llms.keySet().stream().map(it -> new VLModel(it, it))
                    .sorted(Comparator.comparing(VLModel::getLabel)).collect(Collectors.toList());
        }
    }

}
