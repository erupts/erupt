package xyz.erupt.ai.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.constants.ChatSenderType;
import xyz.erupt.ai.constants.SseEvent;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.handler.EruptPromptHandler;
import xyz.erupt.ai.model.AiChat;
import xyz.erupt.ai.model.AiChatMessage;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.model.LLMAgent;
import xyz.erupt.ai.vo.SseBody;
import xyz.erupt.ai.vo.ToolCallRecord;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.jpa.dao.EruptDao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/3/3 22:44
 */
@Service
@Slf4j
public class LLMService {

    @Resource
    private AiProp aiProp;

    @Resource
    private EruptDao eruptDao;

    public String send(String prompt) {
        return this.send(List.of(UserMessage.from(prompt)));
    }

    public String send(List<ChatMessage> chatMessages) {
        return this.send(eruptDao.lambdaQuery(LLM.class).eq(LLM::getDefaultLLM, true).eq(LLM::getEnable, true).limit(1).one(), chatMessages);
    }

    public String send(LLM llm, String prompt) {
        return send(llm, List.of(UserMessage.from(prompt)));
    }

    public String send(LLM llmConfig, List<ChatMessage> chatMessages) {
        if (null == llmConfig) {
            throw new EruptWebApiRuntimeException("Not found LLM config");
        }
        return LlmCore.getLLM(llmConfig.getLlm()).chat(llmConfig.toLlmRequest(), chatMessages);
    }

    @SneakyThrows
    public List<ChatMessage> geneCompletionPrompt(AiChat chat, LLMAgent llmAgent, Integer contextTurn) {
        List<ChatMessage> messages = new ArrayList<>();
        List<AiChatMessage> chatMessages = eruptDao.lambdaQuery(AiChatMessage.class)
                .eq(AiChatMessage::getChatId, chat.getId())
                .isNotNull(AiChatMessage::getContent)
                .orderByDesc(AiChatMessage::getCreatedAt)
                .limit(contextTurn + 1).offset(1).list();
        Collections.reverse(chatMessages);
        for (AiChatMessage message : chatMessages) {
            if (message.getSenderType() == ChatSenderType.USER) {
                messages.add(UserMessage.from(message.getContent()));
            } else if (message.getSenderType() == ChatSenderType.MODEL) {
                if (message.getThinkingContent() != null) {
                    messages.add(AiMessage.builder().text(message.getContent()).thinking(message.getThinkingContent()).build());
                } else {
                    messages.add(AiMessage.from(message.getContent()));
                }
            }
        }
        return messages;
    }

    @SneakyThrows
    public String resolveAgentPrompt(LLMAgent llmAgent) {
        if (null == llmAgent) return null;
        if (null == llmAgent.getPromptHandler()) return llmAgent.getPrompt();
        return EruptSpringUtil.getBeanByPath(llmAgent.getPromptHandler(), EruptPromptHandler.class).handle(llmAgent.getPrompt());
    }

    @Async
    @Transactional
    @SneakyThrows
    public void sendSse(MetaContext metaContext, Boolean autoToolCall, LLMAgent llmAgent, SseEmitter emitter,
                        LlmCore llm, LLM llmModel, AiChatMessage chatMessage,
                        String userMessage, List<ChatMessage> chatContext, String contextPrompt) {
        try {
            MetaContext.set(metaContext);
            LlmRequest llmRequest = llmModel.toLlmRequest();
            if (null != llmAgent) {
                llmAgent.mergeToLLmRequest(llmModel);
                llmRequest.setAgentPrompt(resolveAgentPrompt(llmAgent));
            }
            llmRequest.setAutoCallTool(autoToolCall);
            llmRequest.setContextPrompt(contextPrompt);
            List<ToolCallRecord> toolCallList = new ArrayList<>();
            llm.chatSse(llmRequest, userMessage, chatContext, it -> {
                if (null != it.getToolResult()) {
                    toolCallList.add(new ToolCallRecord(it.getToolName(), it.getToolArgs(), it.getToolResult(), LocalDateTime.now()));
                } else if (null != it.getThrowable()) {
                    String message = it.getThrowable().getMessage();
                    this.sendSseMessage(emitter, message);
                    eruptDao.persistAndFlush(AiChatMessage.create(chatMessage.getChatId(), llmModel.getLlm(), llmModel.getModel(), ChatSenderType.MODEL, message, 0));
                    this.completeSse(emitter);
                } else if (it.isFinish()) {
                    String message = it.getAiMessage() != null && it.getAiMessage().text() != null ? it.getAiMessage().text() : "";
                    String thinking = it.getAiMessage() != null ? it.getAiMessage().thinking() : null;
                    String toolCalls = toolCallList.isEmpty() ? null : GsonFactory.getGson().toJson(toolCallList);
                    this.sendSseDone(emitter);
                    chatMessage.setTokens(it.getUsage().inputTokenCount());
                    eruptDao.mergeAndFlush(chatMessage);
                    eruptDao.persistAndFlush(AiChatMessage.create(chatMessage.getChatId(), llmModel.getLlm(),
                            llmModel.getModel(), ChatSenderType.MODEL, message, thinking, toolCalls, it.getUsage().outputTokenCount()));
                    this.completeSse(emitter);
                } else if (null != it.getCurrMessage()) {
                    this.sendSseMessage(emitter, it.getCurrMessage());
                }
                if (null != it.getCall()) {
                    this.sendSseCallMessage(emitter, it.getCall());
                }
            });
        } catch (Exception e) {
            log.error("LLM error: {}", e.getMessage(), e);
            this.stopSse(emitter, chatMessage, llmModel, e.toString());
        }
    }


    @SneakyThrows
    public void sendSseDone(SseEmitter emitter) {
        this.sendSseBody(emitter, new SseBody(SseEvent.DONE, null));
    }

    @SneakyThrows
    public void sendSseCallClear(SseEmitter emitter) {
        this.sendSseBody(emitter, new SseBody(SseEvent.CALL, null));
    }

    @SneakyThrows
    public void sendSseCallMessage(SseEmitter emitter, String llmMessage) {
        this.sendSseBody(emitter, new SseBody(SseEvent.CALL, llmMessage));
    }

    @SneakyThrows
    private void stopSse(SseEmitter emitter, AiChatMessage chatMessage, LLM llmModel, String reason) {
        eruptDao.persistAndFlush(AiChatMessage.create(chatMessage.getChatId(), llmModel.getLlm(), llmModel.getModel(), ChatSenderType.MODEL, reason, 0));
        this.sendSseBody(emitter, new SseBody(SseEvent.TOKEN, reason));
        this.completeSse(emitter);
    }

    @SneakyThrows
    public void sendSseMessage(SseEmitter emitter, String llmMessage) {
        if (StringUtils.isNotBlank(llmMessage) && llmMessage.length() > aiProp.getMessageChunkSize()) {
            this.sendSseCallClear(emitter);
            for (int i = 0; i < llmMessage.length(); i += aiProp.getMessageChunkSize()) {
                int end = Math.min(i + aiProp.getMessageChunkSize(), llmMessage.length());
                this.sendSseBody(emitter, new SseBody(SseEvent.TOKEN, llmMessage.substring(i, end)));
                if (aiProp.getMessageDelay() > 0) {
                    Thread.sleep(aiProp.getMessageDelay());
                }
            }
        } else {
            this.sendSseBody(emitter, new SseBody(SseEvent.TOKEN, llmMessage));
            this.sendSseCallClear(emitter);
        }
    }

    public void sendSseBody(SseEmitter emitter, SseBody sseBody) {
        try {
            emitter.send(GsonFactory.getGson().toJson(sseBody), MediaType.TEXT_EVENT_STREAM);
        } catch (Exception e) {
            log.warn("SSE send error: {}", e.getMessage());
        }
    }

    public void completeSse(SseEmitter emitter) {
        try {
            emitter.complete();
            log.info("SSE complete");
        } catch (Exception e) {
            log.warn("SSE complete error: {}", e.getMessage());
        }
    }

}
