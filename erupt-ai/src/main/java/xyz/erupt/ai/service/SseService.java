package xyz.erupt.ai.service;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.base.SuperLLM;
import xyz.erupt.ai.constants.ChatSenderType;
import xyz.erupt.ai.model.ChatMessage;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2025/3/3 22:44
 */
@Component
public class SseService {

    @Resource
    private EruptDao eruptDao;

    @Async
    @Transactional
    public void send(SseEmitter emitter, SuperLLM<Object> llm, LLM llmObj, ChatMessage chatMessage, StringBuilder assistantPrompt) {
        llm.chatSse(llmObj, chatMessage.getContent(), assistantPrompt.toString(), it -> {
            try {
                if (it.isFinish()) {
                    chatMessage.setTokens((long) it.getUsage().getPromptTokens());
                    eruptDao.merge(chatMessage);
                    eruptDao.persistAndFlush(ChatMessage.create(chatMessage.getChatId(), ChatSenderType.MODEL, chatMessage.getContent(), (long) it.getUsage().getCompletionTokens()));
                    emitter.complete();
                } else {
                    emitter.send(it.getCurrMessage(), MediaType.TEXT_PLAIN);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
    }

}
