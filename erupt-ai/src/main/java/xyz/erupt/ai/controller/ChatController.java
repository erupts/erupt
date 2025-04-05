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
import xyz.erupt.ai.model.Chat;
import xyz.erupt.ai.model.ChatMessage;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.service.LLMService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.annotation.EruptLoginAuth;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    private LLMService llmService;

    @Resource
    private EruptUserService eruptUserService;

    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    @GetMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Transactional
    public SseEmitter send(@RequestParam Long chatId,
                           @RequestParam String message,
                           @RequestParam(required = false) Long agentId
    ) {
        LLM llmObj = eruptDao.lambdaQuery(LLM.class).eq(LLM::getDefaultLLM, true).limit(1).one();
        SuperLLM<Object> llm = (SuperLLM<Object>) SuperLLM.getLLM(llmObj.getLlm());
        SseEmitter emitter = new SseEmitter();
        ChatMessage chatMessage = ChatMessage.create(chatId, ChatSenderType.USER, message, 0L);
        eruptDao.persist(chatMessage);
        Chat chat = eruptDao.find(Chat.class, chatId);
        llmService.sendSse(MetaContext.get(), emitter, llm, llmObj, chatMessage, llmService.geneCompletionPrompt(chat, agentId));
        return emitter;
    }

    @EruptLoginAuth
    @GetMapping("/create_chat")
    @Transactional
    public R<Long> createChat(@RequestParam String title) {
        Chat chat = new Chat();
        chat.setTitle(title);
        chat.setCreatedTime(LocalDateTime.now());
        chat.setEruptUser(new EruptUserVo(eruptUserService.getCurrentUid()));
        eruptDao.persist(chat);
        return R.ok(chat.getId());
    }

    @EruptLoginAuth
    @GetMapping("/delete_chat")
    @Transactional
    public R<Void> deleteChat(@RequestParam Long chatId) {
        Chat chat = eruptDao.find(Chat.class, chatId);
        chat.setDeleted(true);
        return R.ok();
    }

    @EruptLoginAuth
    @GetMapping("/chats")
    public R<List<Chat>> chats() {
        return R.ok(eruptDao.lambdaQuery(Chat.class)
                .with(Chat::getEruptUser).eq(EruptUserVo::getId, eruptUserService.getCurrentUid()).with()
                .orderByDesc(Chat::getCreatedTime)
                .list());
    }

    @EruptLoginAuth
    @GetMapping("/messages")
    public R<List<ChatMessage>> messages(@RequestParam Long chatId, @RequestParam Integer size,
                                         @RequestParam(defaultValue = "1") Integer index) {
        return R.ok(eruptDao.lambdaQuery(ChatMessage.class)
                .eq(ChatMessage::getChatId, chatId)
                .orderByDesc(ChatMessage::getCreatedAt)
                .offset((index - 1) * size)
                .limit(size)
                .list());
    }

}
