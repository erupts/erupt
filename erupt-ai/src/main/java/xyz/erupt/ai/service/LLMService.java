package xyz.erupt.ai.service;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
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
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.handler.EruptPromptHandler;
import xyz.erupt.ai.model.AiChat;
import xyz.erupt.ai.model.AiChatMessage;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.model.LLMAgent;
import xyz.erupt.ai.tool.AiToolboxManager;
import xyz.erupt.ai.vo.SseBody;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.jpa.dao.EruptDao;

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
                .limit(contextTurn + 1).list();
        Collections.reverse(chatMessages);
        for (AiChatMessage message : chatMessages) {
            if (message.getSenderType() == ChatSenderType.USER) {
                messages.add(UserMessage.from(message.getContent()));
            } else if (message.getSenderType() == ChatSenderType.MODEL) {
                messages.add(AiMessage.from(message.getContent()));
            }
        }
        if (null != llmAgent) {
            // Agent
            if (null == llmAgent.getPromptHandler()) {
                messages.add(SystemMessage.from(llmAgent.getPrompt()));
            } else {
                messages.add(SystemMessage.from(EruptSpringUtil.getBeanByPath(llmAgent.getPromptHandler(), EruptPromptHandler.class).handle(llmAgent.getPrompt())));
            }
        }
        return messages;
    }

    @Async
    @Transactional
    @SneakyThrows
    public void sendSse(MetaContext metaContext, LLMAgent llmAgent, SseEmitter emitter, LlmCore llm, LLM llmModal, AiChatMessage chatMessage, List<ChatMessage> chatMessages) {
        try {
            MetaContext.set(metaContext);
            LlmRequest llmRequest = llmModal.toLlmRequest();
            if (null != llmAgent) {
                llmAgent.mergeToLLmRequest(llmModal);
            }
            llm.chatSse(llmRequest, chatMessages, it -> {
                if (null != it.getThrowable()) {
                    String message = it.getThrowable().getMessage();
                    this.sendSseMessage(emitter, message);
                    eruptDao.persistAndFlush(AiChatMessage.create(chatMessage.getChatId(), llmModal.getLlm(), llmModal.getModel(), ChatSenderType.MODEL, message, 0));
                    emitter.complete();
                } else if (it.isFinish()) {
                    String message = it.getAiMessage().text();
                    if (aiProp.isEnableFunctionCall()) {
                        if (it.getAiMessage().hasToolExecutionRequests()) {
                            List<String> functionCallRtn = new ArrayList<>();
                            for (ToolExecutionRequest request : it.getAiMessage().toolExecutionRequests()) {
                                try {
                                    log.info("Execution tool: {}", request);
                                    Object rtn = AiToolboxManager.invoke(request);
                                    if (null != rtn) {
                                        functionCallRtn.add(rtn.toString());
                                    }
                                } catch (Exception e) {
                                    log.error("Execution tool error: {}, {}", request, e);
                                    this.stopSse(emitter, chatMessage, llmModal, "Execution tool error: " + request.toString() + ", " + e.getMessage());
                                }
                            }
                            if (functionCallRtn.isEmpty()) {
                                message = "Completed !";
                            } else {
                                for (String s : functionCallRtn) {
                                    chatMessages.add(AiMessage.from(s));
                                }
                                message = llm.chat(llmRequest, chatMessages);
                                this.sendSseMessage(emitter, message);
                            }
                        }
                    }
                    chatMessage.setTokens(it.getUsage().inputTokenCount());
                    eruptDao.mergeAndFlush(chatMessage);
                    eruptDao.persistAndFlush(AiChatMessage.create(chatMessage.getChatId(), llmModal.getLlm(), llmModal.getModel(), ChatSenderType.MODEL, message, it.getUsage().outputTokenCount()));
                    emitter.complete();
                } else {
                    // streaming
                    this.sendSseMessage(emitter, it.getCurrMessage());
                }
            });
        } catch (Exception e) {
            this.stopSse(emitter, chatMessage, llmModal, e.toString());
        }
    }

    @SneakyThrows
    private void stopSse(SseEmitter emitter, AiChatMessage chatMessage, LLM llmModal, String reason) {
        eruptDao.persistAndFlush(AiChatMessage.create(chatMessage.getChatId(), llmModal.getLlm(), llmModal.getModel(), ChatSenderType.MODEL, reason, 0));
        emitter.send(GsonFactory.getGson().toJson(new SseBody(reason)), MediaType.TEXT_EVENT_STREAM);
        emitter.complete();
    }

    public void sendSseMessage(SseEmitter emitter, String llmMessage) {
        try {
            if (StringUtils.isNotBlank(llmMessage) && llmMessage.length() > aiProp.getMessageChunkSize()) {
                for (int i = 0; i < llmMessage.length(); i += aiProp.getMessageChunkSize()) {
                    int end = Math.min(i + aiProp.getMessageChunkSize(), llmMessage.length());
                    emitter.send(GsonFactory.getGson().toJson(new SseBody(llmMessage.substring(i, end))), MediaType.TEXT_EVENT_STREAM);
                    if (aiProp.getMessageDelay() > 0) {
                        Thread.sleep(aiProp.getMessageDelay());
                    }
                }
            } else {
                emitter.send(GsonFactory.getGson().toJson(new SseBody(llmMessage)), MediaType.TEXT_EVENT_STREAM);
            }
        } catch (Exception ignore) {
        }
    }

}
