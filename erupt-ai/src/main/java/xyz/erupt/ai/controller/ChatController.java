package xyz.erupt.ai.controller;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.erupt.ai.config.AiProp;
import xyz.erupt.ai.constants.AiConst;
import xyz.erupt.ai.constants.ChatSenderType;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.model.AiChat;
import xyz.erupt.ai.model.AiChatMessage;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.model.LLMAgent;
import xyz.erupt.ai.service.LLMService;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.view.R;
import xyz.erupt.core.view.SimplePage;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.annotation.EruptMenuAuth;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/2/22 16:35
 */
@Slf4j
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/ai/chat")
public class ChatController {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private AiProp aiProp;

    @Resource
    private LLMService llmService;

    @Resource
    private EruptUserService eruptUserService;

    @EruptMenuAuth(AiConst.AI_CHAT)
    @GetMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Transactional
    @SneakyThrows
    public SseEmitter send(@RequestParam Long chatId,
                           @RequestParam String message,
                           @RequestParam(required = false, defaultValue = "true") Boolean autoToolCall,
                           @RequestParam(required = false) Long llmId,
                           @RequestParam(required = false) Long agentId,
                           @RequestParam(required = false) String contextPrompt
    ) {
        LLM llmModel;
        if (llmId == null) {
            llmModel = eruptDao.lambdaQuery(LLM.class).eq(LLM::getDefaultLLM, true).eq(LLM::getEnable, true).limit(1).one();
        } else {
            llmModel = eruptDao.find(LLM.class, llmId);
        }
        SseEmitter emitter = new SseEmitter(aiProp.getSseTimeout());
        if (null == llmModel) {
            llmService.sendSseMessage(emitter, "No LLM available");
            llmService.completeSse(emitter);
            return emitter;
        }
        eruptDao.detach(llmModel);
        emitter.onTimeout(() -> {
            log.info("Sse Request timed out chatId: {}", chatId);
            llmService.sendSseMessage(emitter, "Request timed out, please try again");
        });
        emitter.onError((throwable) -> log.error("Sse Request failed chatId: {}", chatId, throwable));
        if (message.isBlank()) {
            llmService.sendSseMessage(emitter, "Please enter a prompt");
            llmService.completeSse(emitter);
            return emitter;
        } else {
            LlmCore llm = LlmCore.getLLM(llmModel.getLlm());
            AiChatMessage chatMessage = AiChatMessage.create(chatId, llmModel.getLlm(), llmModel.getModel(), ChatSenderType.USER, message, 0);
            chatMessage.setAgentId(agentId);
            eruptDao.persist(chatMessage);
            AiChat chat = eruptDao.find(AiChat.class, chatId);
            LLMAgent llmAgent = null;
            if (null != agentId) {
                llmAgent = eruptDao.find(LLMAgent.class, agentId);
            }
            llmService.sendSse(MetaContext.get(), autoToolCall, llmAgent, emitter, llm, llmModel, chatMessage,
                    message, llmService.geneCompletionPrompt(chat, llmAgent, llmModel.getMaxContext()), contextPrompt);
        }

        return emitter;
    }

    @EruptMenuAuth(AiConst.AI_CHAT)
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

    @EruptMenuAuth(AiConst.AI_CHAT)
    @GetMapping("/delete-chat")
    @Transactional
    public R<Void> deleteChat(@RequestParam Long chatId) {
        AiChat chat = eruptDao.find(AiChat.class, chatId);
        chat.setDeleted(true);
        return R.ok();
    }

    @EruptMenuAuth(AiConst.AI_CHAT)
    @PostMapping("/rename-chat")
    @Transactional
    public R<Void> renameChat(@RequestParam Long chatId, @RequestParam String title) {
        AiChat chat = eruptDao.find(AiChat.class, chatId);
        chat.setTitle(title);
        eruptDao.persist(chat);
        return R.ok();
    }


    @EruptMenuAuth(AiConst.AI_CHAT)
    @GetMapping("/chats")
    public R<SimplePage<AiChat>> chats(@RequestParam Integer size,
                                       @RequestParam Integer index) {
        return R.ok(eruptDao.lambdaQuery(AiChat.class)
                .with(AiChat::getEruptUser).eq(EruptUserVo::getId, eruptUserService.getCurrentUid()).with()
                .orderByDesc(AiChat::getCreatedTime)
                .page(size, (index - 1) * size));
    }

    @EruptMenuAuth(AiConst.AI_CHAT)
    @GetMapping("/messages")
    public R<List<AiChatMessage>> messages(@RequestParam Long chatId,
                                           @RequestParam Integer size,
                                           @RequestParam Integer index) {
        return R.ok(eruptDao.lambdaQuery(AiChatMessage.class)
                .eq(AiChatMessage::getChatId, chatId)
                .orderByDesc(AiChatMessage::getCreatedAt)
                .offset((index - 1) * size)
                .limit(size)
                .list());
    }

}
