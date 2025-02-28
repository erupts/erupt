package xyz.erupt.ai.controller;

import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.base.SuperLLM;
import xyz.erupt.ai.constants.ChatSenderType;
import xyz.erupt.ai.constants.ChatType;
import xyz.erupt.ai.model.Chat;
import xyz.erupt.ai.model.ChatMessage;
import xyz.erupt.ai.model.LLMAgent;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author YuePeng
 * date 2025/2/22 16:35
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/ai/chat")
public class ChatController {

    private final ExecutorService sseExecutorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            Runtime.getRuntime().availableProcessors() * 4,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));

    @Resource
    private EruptDao eruptDao;

    @GetMapping("/send")
    @Transactional
    public SseEmitter send(@RequestParam Long chatId, @RequestParam String llmCode, @RequestParam String message) {
        SuperLLM<Object> llm = (SuperLLM<Object>) SuperLLM.getLLM(llmCode);
        if (llm == null) throw new RuntimeException("llm not found");
        StringBuilder assistantPrompt = new StringBuilder();
        SseEmitter emitter = new SseEmitter();
        eruptDao.persist(ChatMessage.create(chatId, ChatSenderType.USER, message));

        Chat chat = eruptDao.find(Chat.class, chatId);
        if (chat.getType() == ChatType.AGENT) {
            LLMAgent llmAgent = new LLMAgent();
            assistantPrompt.append(llmAgent.getPrompt());
        } else {
            assistantPrompt.append("回答不超过10个字");
        }

        sseExecutorService.submit(() -> llm.chatSse(llm.config(), message, assistantPrompt.toString(), (it) -> {
            try {
                if (it.isFinish()) {
                    eruptDao.persistAndFlush(ChatMessage.create(chatId, ChatSenderType.MODEL, message));
                    emitter.complete();
                } else {
                    emitter.send(it.getCurrMessage(), MediaType.TEXT_PLAIN);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }));
        return emitter;
    }

    @GetMapping("/create_chat")
    @Transactional
    public void createChat() {
        Chat chat = new Chat();
        chat.setType(ChatType.NORMAL);
        chat.setUserId(Long.valueOf(MetaContext.getUser().getUid()));
        eruptDao.persist(chat);
    }

    @GetMapping("/chats")
    public R<List<Chat>> chats() {
        return R.ok(eruptDao.lambdaQuery(Chat.class)
                .eq(Chat::getUserId, Long.valueOf(MetaContext.getUser().getUid()))
                .eq(Chat::getType, ChatType.NORMAL)
                .orderByDesc(Chat::getCreatedTime)
                .list());
    }

    @GetMapping("/messages")
    public R<List<ChatMessage>> messages(@RequestParam Long chatId, @RequestParam Integer size,
                                         @RequestParam(defaultValue = "0") Integer index) {
        return R.ok(eruptDao.lambdaQuery(ChatMessage.class)
                .eq(ChatMessage::getChatId, chatId)
                .orderByDesc(ChatMessage::getCreateTime)
                .offset(index * size)
                .limit(size)
                .list());
    }

}
