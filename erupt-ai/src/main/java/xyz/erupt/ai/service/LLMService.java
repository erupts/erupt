package xyz.erupt.ai.service;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.tool.AiToolboxManager;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.constants.ChatSenderType;
import xyz.erupt.ai.constants.MessageRole;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.handler.EruptPromptHandler;
import xyz.erupt.ai.model.Chat;
import xyz.erupt.ai.model.ChatMessage;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.model.LLMAgent;
import xyz.erupt.ai.pojo.ChatCompletionMessage;
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
        return send(prompt, Collections.emptyList());
    }

    public String send(String prompt, List<ChatCompletionMessage> assistantPrompt) {
        return send(eruptDao.lambdaQuery(LLM.class).eq(LLM::getDefaultLLM, true).eq(LLM::getEnable, true).limit(1).one(), prompt, assistantPrompt);
    }

    public String send(LLM llm, String prompt) {
        return send(llm, prompt, Collections.emptyList());
    }

    public String send(LLM llmConfig, String prompt, List<ChatCompletionMessage> assistantPrompt) {
        if (null == llmConfig) {
            throw new EruptWebApiRuntimeException("Not found LLM config");
        }
        return LlmCore.getLLM(llmConfig.getLlm()).chat(llmConfig.toLlmRequest(), prompt, assistantPrompt);
    }

    @SneakyThrows
    public List<ChatCompletionMessage> geneCompletionPrompt(Chat chat, LLMAgent llmAgent, Integer contextTurn) {
        List<ChatCompletionMessage> chatCompletionMessages = new ArrayList<>();
        chatCompletionMessages.add(new ChatCompletionMessage(MessageRole.system, aiProp.getSystemPrompt()));
        if (null != llmAgent) {
            // Agent
            if (null == llmAgent.getPromptHandler()) {
                chatCompletionMessages.add(new ChatCompletionMessage(MessageRole.system, llmAgent.getPrompt()));
            } else {
                chatCompletionMessages.add(new ChatCompletionMessage(MessageRole.system,
                        EruptSpringUtil.getBeanByPath(llmAgent.getPromptHandler(), EruptPromptHandler.class).handle(llmAgent.getPrompt())));
            }
        }
        List<ChatMessage> chatMessages = eruptDao.lambdaQuery(ChatMessage.class)
                .eq(ChatMessage::getChatId, chat.getId())
                .isNotNull(ChatMessage::getContent)
                .orderByDesc(ChatMessage::getCreatedAt)
                .limit(contextTurn + 1).list();
        Collections.reverse(chatMessages);
        chatMessages.forEach(it -> chatCompletionMessages.add(
                new ChatCompletionMessage(it.getSenderType() == ChatSenderType.USER ? MessageRole.user : MessageRole.assistant, it.getContent()))
        );
        return chatCompletionMessages;
    }

    @Async
    @Transactional
    @SneakyThrows
    public void sendSse(MetaContext metaContext, LLMAgent llmAgent, SseEmitter emitter, LlmCore llm, LLM llmModal, ChatMessage chatMessage, List<ChatCompletionMessage> completionMessage) {
        try {
            MetaContext.set(metaContext);
            LlmRequest llmRequest = llmModal.toLlmRequest();
            if (null != llmAgent) {
                llmAgent.mergeToLLmRequest(llmModal);
            }
            completionMessage.removeIf(it -> StringUtils.isBlank(it.getContent()));
            llm.chatSse(llmRequest, chatMessage.getContent(), completionMessage, it -> {
                if (null != it.getThrowable()) {
                    String message = it.getThrowable().getMessage();
                    this.sendSseMessage(emitter, message);
                    eruptDao.persistAndFlush(ChatMessage.create(chatMessage.getChatId(), llmModal.getLlm(), llmModal.getModel(), ChatSenderType.MODEL, message, 0));
                    emitter.complete();
                } else if (it.isFinish()) {
                    String message = it.getAiMessage().text();
                    if (aiProp.isEnableFunctionCall()) {
                        List<String> functionCallRtn = new ArrayList<>();
                        if (it.getAiMessage().hasToolExecutionRequests()) {
                            for (ToolExecutionRequest request : it.getAiMessage().toolExecutionRequests()) {
                                Object rtn = AiToolboxManager.invoke(request);
                                if (null != rtn) {
                                    functionCallRtn.add(rtn.toString());
                                }
                            }
                        }
                        if (!functionCallRtn.isEmpty()) {
                            for (String s : functionCallRtn) {
                                completionMessage.add(new ChatCompletionMessage(MessageRole.tool, s));
                            }
                            message = llm.chat(llmRequest, chatMessage.getContent(), completionMessage);
                            this.sendSseMessage(emitter, message);
                        }else{
                            message = "Completed";
                        }
                    }
                    chatMessage.setTokens(it.getUsage().inputTokenCount());
                    eruptDao.mergeAndFlush(chatMessage);
                    eruptDao.persistAndFlush(ChatMessage.create(chatMessage.getChatId(), llmModal.getLlm(), llmModal.getModel(), ChatSenderType.MODEL, message, it.getUsage().outputTokenCount()));
                    emitter.complete();
                } else {
                    // streaming
                    this.sendSseMessage(emitter, it.getCurrMessage());
                }
            });
        } catch (Exception e) {
            String message = e.toString();
            eruptDao.persistAndFlush(ChatMessage.create(chatMessage.getChatId(), llmModal.getLlm(), llmModal.getModel(), ChatSenderType.MODEL, message, 0));
            emitter.send(GsonFactory.getGson().toJson(new SseBody(message)), MediaType.TEXT_EVENT_STREAM);
            emitter.complete();
        }
    }

    private void sendSseMessage(SseEmitter emitter, String llmMessage) {
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
