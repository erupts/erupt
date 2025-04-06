package xyz.erupt.ai.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.base.SuperLLM;
import xyz.erupt.ai.call.AiFunctionManager;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.constants.ChatSenderType;
import xyz.erupt.ai.constants.MessageRole;
import xyz.erupt.ai.handler.EruptPromptHandler;
import xyz.erupt.ai.model.Chat;
import xyz.erupt.ai.model.ChatMessage;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.model.LLMAgent;
import xyz.erupt.ai.pojo.ChatCompletionMessage;
import xyz.erupt.ai.vo.SseBody;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    @Resource
    private AiFunctionManager aiFunctionManager;

    public static final int MESSAGE_TS = 100;

    public List<ChatCompletionMessage> geneCompletionPrompt(Chat chat, Long agentId) {
        List<ChatCompletionMessage> chatCompletionMessages = new ArrayList<>();
        chatCompletionMessages.add(new ChatCompletionMessage(MessageRole.system, aiProp.getSystemPrompt()));
        if (null != agentId) {
            LLMAgent llmAgent = eruptDao.find(LLMAgent.class, agentId);
            if (null == llmAgent.getPromptHandler()) {
                chatCompletionMessages.add(new ChatCompletionMessage(MessageRole.system, llmAgent.getPrompt()));
            } else {
                chatCompletionMessages.add(new ChatCompletionMessage(MessageRole.system,
                        EruptSpringUtil.getBean(llmAgent.getPromptHandler(), EruptPromptHandler.class).handle(llmAgent.getPrompt())));
            }
        } else {
            chatCompletionMessages.add(new ChatCompletionMessage(MessageRole.assistant, aiFunctionManager.getFunctionCallPrompt()));
        }
        List<ChatMessage> chatMessages = eruptDao.lambdaQuery(ChatMessage.class)
                .eq(ChatMessage::getChatId, chat.getId()).eq(ChatMessage::getSenderType, ChatSenderType.USER)
                .orderByDesc(ChatMessage::getCreatedAt)
                .limit(20).list();
        chatMessages.forEach(it -> chatCompletionMessages.add(new ChatCompletionMessage(MessageRole.user, it.getContent())));
        return chatCompletionMessages;
    }

    @Async
    @Transactional
    @SneakyThrows
    public void sendSse(MetaContext metaContext, SseEmitter emitter, SuperLLM llm, LLM llmObj, ChatMessage chatMessage, List<ChatCompletionMessage> completionMessage) {
        try {
            MetaContext.set(metaContext);
            llm.chatSse(llmObj, chatMessage.getContent(), completionMessage, it -> {
                if (it.isFinish()) {
                    String msg = it.getOutput().toString();
                    if (it.getOutput().toString().length() <= MESSAGE_TS) {
                        msg = sendMessage(emitter, it.getOutput().toString());
                    }
                    chatMessage.setTokens((long) it.getUsage().getPrompt_tokens());
                    eruptDao.mergeAndFlush(chatMessage);
                    eruptDao.persistAndFlush(ChatMessage.create(chatMessage.getChatId(), ChatSenderType.MODEL, msg, (long) it.getUsage().getCompletion_tokens()));
                    emitter.complete();
                } else {
                    if (it.getOutput().toString().length() > MESSAGE_TS) {
                        if (it.isPending()) {
                            sendMessage(emitter, it.getOutput().toString());
                            it.setPending(false);
                        } else {
                            sendMessage(emitter, it.getCurrMessage());
                        }
                    } else {
                        it.setPending(true);
                    }
                }
            });
        } catch (Exception e) {
            eruptDao.persistAndFlush(ChatMessage.create(chatMessage.getChatId(), ChatSenderType.MODEL, e.getMessage(), 0L));
            emitter.send(GsonFactory.getGson().toJson(new SseBody(e.getMessage())), MediaType.TEXT_EVENT_STREAM);
            emitter.complete();
        }
    }

    private String sendMessage(SseEmitter emitter, String message) {
        if (aiFunctionManager.exist(message.trim())) {
            String functionMessage = aiFunctionManager.call(message);
            try {
                emitter.send(GsonFactory.getGson().toJson(new SseBody(functionMessage)), MediaType.TEXT_EVENT_STREAM);
            } catch (Exception ignore) {
            }
            return functionMessage;
        } else {
            try {
                emitter.send(GsonFactory.getGson().toJson(new SseBody(message)), MediaType.TEXT_EVENT_STREAM);
            } catch (Exception ignore) {
            }
        }
        return message;
    }

}
