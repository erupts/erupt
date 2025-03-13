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
import xyz.erupt.ai.handler.EruptPromptHandler;
import xyz.erupt.ai.model.Chat;
import xyz.erupt.ai.model.ChatMessage;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.model.LLMAgent;
import xyz.erupt.ai.service.SseService;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/2/22 16:35
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/ai/chat")
public class ChatController {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private SseService sseService;

    @GetMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Transactional
    public SseEmitter send(@RequestParam Long chatId, @RequestParam String message) {
        LLM llmObj = eruptDao.lambdaQuery(LLM.class).eq(LLM::getDefaultLLM, true).limit(1).one();
        SuperLLM<Object> llm = (SuperLLM<Object>) SuperLLM.getLLM(llmObj.getLlm());
        StringBuilder assistantPrompt = new StringBuilder();
        SseEmitter emitter = new SseEmitter();
        ChatMessage chatMessage = ChatMessage.create(chatId, ChatSenderType.USER, message, 0L);
        eruptDao.persist(chatMessage);
        Chat chat = eruptDao.find(Chat.class, chatId);
        if (chat.getType() == ChatType.AGENT) {
            LLMAgent llmAgent = new LLMAgent();
            if (null == llmAgent.getPromptHandler()) {
                assistantPrompt.append(llmAgent.getPrompt());
            } else {
                assistantPrompt.append(EruptSpringUtil.getBean(llmAgent.getPromptHandler(), EruptPromptHandler.class).handle(llmAgent.getPrompt()));
            }
        } else {
            assistantPrompt.append("回答不超过100个字");
        }
        emitter.onCompletion(() -> {
            System.out.println("SseEmitter completed");
        });

        // 设置超时回调
        emitter.onTimeout(() -> {
            System.out.println("SseEmitter timed out");
        });
        // 设置异常回调
        emitter.onError(e -> {
            System.out.println("SseEmitter error: " + e.getMessage());
        });
        sseService.send(emitter, llm, llmObj, chatMessage, assistantPrompt);
        return emitter;
    }

    @GetMapping("/create_chat")
    @Transactional
    public void createChat(String title) {
        Chat chat = new Chat();
        chat.setType(ChatType.NORMAL);
        chat.setUserId(Long.valueOf(MetaContext.getUser().getUid()));
        eruptDao.persist(chat);
    }

    @GetMapping("/chats")
    public R<List<Chat>> chats() {
        return R.ok(eruptDao.lambdaQuery(Chat.class)
                .eq(null != MetaContext.getUser().getUid(), Chat::getUserId, null == MetaContext.getUser().getUid() ? null : Long.valueOf(MetaContext.getUser().getUid()))
                .eq(Chat::getType, ChatType.NORMAL)
                .orderByDesc(Chat::getCreatedTime)
                .list());
    }

    @GetMapping("/messages")
    public R<List<ChatMessage>> messages(@RequestParam Long chatId,
                                         @RequestParam Integer size,
                                         @RequestParam(defaultValue = "0") Integer index) {
        return R.ok(eruptDao.lambdaQuery(ChatMessage.class)
                .eq(ChatMessage::getChatId, chatId)
                .orderByDesc(ChatMessage::getCreateTime)
                .offset(index * size)
                .limit(size)
                .list());
    }

}
