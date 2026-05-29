package xyz.erupt.ai.core;

import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolErrorHandlerResult;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderResult;
import lombok.extern.slf4j.Slf4j;
import xyz.erupt.ai.ask.EruptAiChat;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.service.LLMRoleService;
import xyz.erupt.ai.service.McpServerService;
import xyz.erupt.ai.vo.mcp.McpClientInfo;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.prompt.SystemPromptProvider;
import xyz.erupt.core.util.EruptSpringUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
        return this.buildAiServices(eruptAiServices, llmRequest, null).chat(userMessage).text();
    }

    public void chatSse(LlmRequest llmRequest, String userMessage, List<ChatMessage> chatContext, Consumer<SseListener> listener) {
        StreamingChatModel streamingChatModel = this.buildStreamingChatModel(llmRequest, chatContext, listener);
        ChatMemory chatMemory = creatMemory(chatContext);
        AiServices<EruptAiChat> eruptAiServices = AiServices.builder(EruptAiChat.class)
                .streamingChatModel(streamingChatModel).chatMemoryProvider((id) -> chatMemory);
        MetaContext metaContext = MetaContext.get();
        this.streamingChat(this.buildAiServices(eruptAiServices, llmRequest, listener), userMessage, metaContext, listener);
    }

    private EruptAiChat buildAiServices(AiServices<EruptAiChat> eruptAiServices, LlmRequest llmRequest, Consumer<SseListener> listener) {
        eruptAiServices.systemMessageProvider((id) -> {
            AiProp aiProp = EruptSpringUtil.getBean(AiProp.class);
            StringBuffer systemPrompt = new StringBuffer(aiProp.getSystemPrompt());
            SystemPromptProvider.getRegisteredProviders().forEach(provider ->
                    systemPrompt.append("\n\n").append(provider.getPrompt()));
            String rolePrompt = EruptSpringUtil.getBean(LLMRoleService.class).getSystemPromptByUid(MetaContext.getUser().getUid());
            if (rolePrompt != null && !rolePrompt.isBlank()) {
                systemPrompt.append("\n\n").append(rolePrompt);
            }
            return systemPrompt.toString();
        });
        if (llmRequest.getAutoCallTool()) {
            eruptAiServices.toolProvider(buildTools(listener));
        }
        eruptAiServices.toolExecutionErrorHandler((throwable, e) -> {
            log.error("Tool execution error [{}] e: {}", throwable.getMessage(), e);
            return new ToolErrorHandlerResult("Tool error: " + e.toString());
        });
        return eruptAiServices.build();
    }

    private ToolProvider buildTools(Consumer<SseListener> listener) {
        return (request) -> {
            ToolProviderResult.Builder builder = ToolProviderResult.builder();
            Set<String> allowedTools = EruptSpringUtil.getBean(LLMRoleService.class)
                    .getAllowedToolsByUid(MetaContext.getUser().getUid());

            AiToolboxManager.getTools().forEach(obj ->
                    ToolSpecifications.toolSpecificationsFrom(obj).forEach(spec -> {
                        if (allowedTools.contains(spec.name())) {
                            builder.add(spec, (executionRequest, memoryId) -> {
                                Object result = AiToolboxManager.invoke(executionRequest);
                                String resultStr = null == result ? "" : result.toString();
                                if (listener != null) {
                                    listener.accept(SseListener.builder()
                                            .toolName(executionRequest.name())
                                            .toolArgs(executionRequest.arguments())
                                            .toolResult(resultStr).build());
                                }
                                return resultStr;
                            });
                        }
                    }));
            for (McpClientInfo value : McpServerService.getMCP_CLIENTS().values()) {
                if (null != value.getMcpClient()) {
                    value.getMcpClient().listTools().forEach(spec ->
                            builder.add(spec, (executionRequest, memoryId) -> {
                                String result = value.getMcpClient().executeTool(executionRequest).toString();
                                if (listener != null) {
                                    listener.accept(SseListener.builder()
                                            .toolName(executionRequest.name())
                                            .toolArgs(executionRequest.arguments())
                                            .toolResult(result).build());
                                }
                                return result;
                            })
                    );
                }
            }
            return builder.build();
        };
    }

    private void streamingChat(EruptAiChat eruptAiChat, String userMessage, MetaContext metaContext, Consumer<SseListener> listener) {
        MetaContext.set(metaContext);
        AtomicBoolean toolCalling = new AtomicBoolean(false);
        eruptAiChat.streamChat(userMessage).onPartialResponse(partialResponse -> {
                    toolCalling.set(true);
                    listener.accept(SseListener.builder().currMessage(partialResponse).build());
                })
                .onCompleteResponse(chatResponse -> {
                    toolCalling.set(false);
                    listener.accept(SseListener.builder()
                            .isFinish(true)
                            .usage(chatResponse.tokenUsage())
                            .aiMessage(chatResponse.aiMessage()).build());
                })
                .onError(e -> {
                    log.error("Failed to get response from server", e);
                    if (!toolCalling.get()) {
                        listener.accept(SseListener.builder().isFinish(true).throwable(e).build());
                    }
                }).onPartialToolCall(toolCall -> {
                    toolCalling.set(true);
                    MetaContext.set(metaContext);
                    String callMsg = toolCall.name();
                    listener.accept(SseListener.builder().call(callMsg).build());
                }).onPartialThinking(thinking -> {
                    listener.accept(SseListener.builder().currMessage(thinking.text()).build());
                }).start();
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
