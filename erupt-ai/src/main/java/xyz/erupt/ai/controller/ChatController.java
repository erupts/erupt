package xyz.erupt.ai.controller;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.constants.ChatSenderType;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.model.AiChat;
import xyz.erupt.ai.model.AiChatMessage;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.model.LLMAgent;
import xyz.erupt.ai.service.LLMService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.view.R;
import xyz.erupt.core.view.SimplePage;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.annotation.EruptLoginAuth;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

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
    @SneakyThrows
    public SseEmitter send(@RequestParam Long chatId,
                           @RequestParam String message,
                           @RequestParam(required = false) Long llmId,
                           @RequestParam(required = false) Long agentId
    ) {
        LLM llmModel;
        if (llmId == null) {
            llmModel = eruptDao.lambdaQuery(LLM.class).eq(LLM::getDefaultLLM, true).eq(LLM::getEnable, true).limit(1).one();
        } else {
            llmModel = eruptDao.find(LLM.class, llmId);
        }
        SseEmitter emitter = new SseEmitter();
        if (message.isBlank()) {
            llmService.sendSseMessage(emitter, "Please enter a prompt");
            emitter.complete();
            return emitter;
        } else if (llmModel == null) {
            llmService.sendSseMessage(emitter, "No LLM available");
            emitter.complete();
        } else {
            LlmCore llm = LlmCore.getLLM(llmModel.getLlm());
            AiChatMessage chatMessage = AiChatMessage.create(chatId, llmModel.getLlm(), llmModel.getModel(), ChatSenderType.USER, message, 0);
            eruptDao.persist(chatMessage);
            AiChat chat = eruptDao.find(AiChat.class, chatId);
            LLMAgent llmAgent = null;
            if (null != agentId) {
                llmAgent = eruptDao.find(LLMAgent.class, agentId);
            }
            llmService.sendSse(MetaContext.get(), llmAgent, emitter, llm, llmModel, chatMessage, llmService.geneCompletionPrompt(chat, llmAgent, llmModel.getMaxContext()));
        }

        return emitter;
    }

    @EruptLoginAuth
    @PostMapping("/create-chat")
    @Transactional
    public R<Long> createChat(@RequestParam String title) {
        AiChat chat = new AiChat();
        if (title.length() > 100) title = title.substring(0, 100);
        chat.setTitle(title);
        chat.setCreatedTime(LocalDateTime.now());
        chat.setEruptUser(new EruptUserVo(eruptUserService.getCurrentUid()));
        eruptDao.persist(chat);
        return R.ok(chat.getId());
    }

    @EruptLoginAuth
    @GetMapping("/delete-chat")
    @Transactional
    public R<Void> deleteChat(@RequestParam Long chatId) {
        AiChat chat = eruptDao.find(AiChat.class, chatId);
        chat.setDeleted(true);
        return R.ok();
    }

    @EruptLoginAuth
    @PostMapping("/rename-chat")
    @Transactional
    public R<Void> renameChat(@RequestParam Long chatId, @RequestParam String title) {
        AiChat chat = eruptDao.find(AiChat.class, chatId);
        chat.setTitle(title);
        eruptDao.persist(chat);
        return R.ok();
    }


    @EruptLoginAuth
    @GetMapping("/chats")
    public R<SimplePage<AiChat>> chats(@RequestParam Integer size,
                                       @RequestParam(defaultValue = "1") Integer index) {
        return R.ok(eruptDao.lambdaQuery(AiChat.class)
                .with(AiChat::getEruptUser).eq(EruptUserVo::getId, eruptUserService.getCurrentUid()).with()
                .orderByDesc(AiChat::getCreatedTime)
                .page(size, (index - 1) * size));
    }

    @EruptLoginAuth
    @GetMapping("/messages")
    public R<List<AiChatMessage>> messages(@RequestParam Long chatId, @RequestParam Integer size,
                                           @RequestParam(defaultValue = "1") Integer index) {
        return R.ok(eruptDao.lambdaQuery(AiChatMessage.class)
                .eq(AiChatMessage::getChatId, chatId)
                .orderByDesc(AiChatMessage::getCreatedAt)
                .offset((index - 1) * size)
                .limit(size)
                .list());
    }

}
