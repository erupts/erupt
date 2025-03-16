package xyz.erupt.ai.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.base.SuperLLM;
import xyz.erupt.ai.call.AiFunctionManager;
import xyz.erupt.ai.constants.ChatSenderType;
import xyz.erupt.ai.model.ChatMessage;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.vo.SseBody;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/3/3 22:44
 */
@Component
@Slf4j
public class SseService {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private AiFunctionManager aiFunctionManager;

    public static final int MESSAGE_TS = 100;

    @Async
    @Transactional
    @SneakyThrows
    public void send(SseEmitter emitter, SuperLLM<Object> llm, LLM llmObj, ChatMessage chatMessage, List<String> assistantPrompt) {
        try {
            llm.chatSse(llmObj, chatMessage.getContent(), assistantPrompt, it -> {
                if (it.isFinish()) {
                    String msg = it.getOutput().toString();
                    if (it.getOutput().toString().length() <= MESSAGE_TS) {
                        msg = sendMessage(emitter, it.getOutput().toString());
                    }
                    chatMessage.setTokens((long) it.getUsage().getPrompt_tokens());
                    eruptDao.merge(chatMessage);
                    eruptDao.persist(ChatMessage.create(chatMessage.getChatId(), ChatSenderType.MODEL, msg, (long) it.getUsage().getCompletion_tokens()));
                    emitter.complete();
                } else {
                    if (it.getOutput().toString().length() > MESSAGE_TS) {
                        sendMessage(emitter, it.getCurrMessage());
                    }
                }
            });
        } catch (Exception e) {
            eruptDao.persist(ChatMessage.create(chatMessage.getChatId(), ChatSenderType.MODEL, e.getMessage(), 0L));
            emitter.send(GsonFactory.getGson().toJson(new SseBody(e.getMessage())), MediaType.TEXT_EVENT_STREAM);
            emitter.completeWithError(e);
        }
    }

    @SneakyThrows
    private String sendMessage(SseEmitter emitter, String message) {
        if (aiFunctionManager.exist(message)) {
            String functionMessage = aiFunctionManager.call(message);
            emitter.send(GsonFactory.getGson().toJson(new SseBody(functionMessage)), MediaType.TEXT_EVENT_STREAM);
            return functionMessage;
        } else {
            emitter.send(GsonFactory.getGson().toJson(new SseBody(message)), MediaType.TEXT_EVENT_STREAM);
        }
        return message;
    }

}
