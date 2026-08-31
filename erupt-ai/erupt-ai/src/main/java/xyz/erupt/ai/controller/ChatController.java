package xyz.erupt.ai.controller;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
import xyz.erupt.ai.vo.LlmVo;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.service.EruptFileService;
import xyz.erupt.core.view.R;
import xyz.erupt.core.view.SimplePage;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.annotation.EruptMenuAuth;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptUserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    @Resource
    private EruptFileService eruptFileService;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    @EruptMenuAuth(AiConst.AI_CHAT)
    @GetMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Transactional
    @SneakyThrows
    public SseEmitter send(@RequestParam("chatId") Long chatId,
                           @RequestParam("message") String message,
                           @RequestParam(value = "autoToolCall", required = false, defaultValue = "true") Boolean autoToolCall,
                           @RequestParam(value = "llmId", required = false) Long llmId,
                           @RequestParam(value = "agentId", required = false) Long agentId,
                           @RequestParam(value = "contextPrompt", required = false) String contextPrompt,
                           // JSON array of attachment paths returned by /upload-image
                           @RequestParam(value = "images", required = false) String images
    ) {
        LLMAgent llmAgent = agentId == null ? null : eruptDao.find(LLMAgent.class, agentId);
        LLM llmModel;
        if (llmAgent != null && llmAgent.getLlm() != null) {
            // The expert pins its own model, overriding the chat's selection
            llmModel = eruptDao.find(LLM.class, llmAgent.getLlm().getId());
        } else if (llmId == null) {
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
        if (message.isBlank() && StringUtils.isBlank(images)) {
            llmService.sendSseMessage(emitter, "Please enter a prompt");
            llmService.completeSse(emitter);
            return emitter;
        } else {
            LlmCore llm = LlmCore.getLLM(llmModel.getLlm());
            AiChatMessage chatMessage = AiChatMessage.create(chatId, llmModel.getLlm(), llmModel.getModel(), ChatSenderType.USER, message, 0);
            chatMessage.setAgentId(agentId);
            chatMessage.setImages(checkImages(images));
            eruptDao.persist(chatMessage);
            AiChat chat = eruptDao.find(AiChat.class, chatId);
            llmService.sendSse(MetaContext.get(), autoToolCall, llmAgent, emitter, llm, llmModel, chatMessage,
                    message, llmService.geneCompletionPrompt(chat, llmAgent, llmModel.getMaxContext()), contextPrompt);
        }

        return emitter;
    }

    // Validate the images param is a JSON array of image attachment paths before it is persisted
    private String checkImages(String images) {
        if (StringUtils.isBlank(images)) return null;
        String[] paths = GsonFactory.getGson().fromJson(images, String[].class);
        for (String path : paths) {
            if (!IMAGE_EXTENSIONS.contains(StringUtils.substringAfterLast(path, ".").toLowerCase())) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("Only image files are supported"));
            }
        }
        return images;
    }

    @EruptMenuAuth(AiConst.AI_CHAT)
    @PostMapping("/upload-image")
    public R<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".").toLowerCase();
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            return R.error(I18nTranslate.$translate("Only image files are supported"));
        }
        return R.ok(eruptFileService.upload(file, eruptFileService.createPath(file)));
    }

    @EruptMenuAuth(AiConst.AI_CHAT)
    @PostMapping("/create-chat")
    @Transactional
    public R<Long> createChat(@RequestParam("title") String title) {
        AiChat chat = new AiChat();
        if (title.length() > 100) title = title.substring(0, 100);
        chat.setTitle(title);
        chat.setCreatedTime(LocalDateTime.now());
        chat.setEruptUser(new EruptUserVo(eruptUserService.getCurrentUid()));
        eruptDao.persist(chat);
        return R.ok(chat.getId());
    }

    @EruptMenuAuth(AiConst.AI_CHAT)
    @GetMapping("/stop")
    public R<Void> stop(@RequestParam("chatId") Long chatId) {
        AiChat chat = eruptDao.lambdaQuery(AiChat.class)
                .eq(AiChat::getId, chatId)
                .with(AiChat::getEruptUser).eq(EruptUserVo::getId, eruptUserService.getCurrentUid()).with()
                .one();
        if (null == chat) return R.error("Chat not found");
        // Signal is polled by the generating instance; works across nodes when redisSession is on
        llmService.stopChat(chatId);
        return R.ok();
    }

    @EruptMenuAuth(AiConst.AI_CHAT)
    @GetMapping("/delete-chat")
    @Transactional
    public R<Void> deleteChat(@RequestParam("chatId") Long chatId) {
        AiChat chat = eruptDao.find(AiChat.class, chatId);
        chat.setDeleted(true);
        return R.ok();
    }

    @EruptMenuAuth(AiConst.AI_CHAT)
    @PostMapping("/rename-chat")
    @Transactional
    public R<Void> renameChat(@RequestParam("chatId") Long chatId, @RequestParam("title") String title) {
        AiChat chat = eruptDao.find(AiChat.class, chatId);
        chat.setTitle(title);
        eruptDao.persist(chat);
        return R.ok();
    }


    // Enabled models for the chat model picker; locked models are never exposed
    // and credentials stay server-side
    @EruptMenuAuth(AiConst.AI_CHAT)
    @GetMapping("/llms")
    public R<List<LlmVo>> llms() {
        return R.ok(eruptDao.lambdaQuery(LLM.class).eq(LLM::getEnable, true)
                .orderByAsc(LLM::getSort).list().stream().map(it -> {
                    LlmVo llmVo = new LlmVo();
                    llmVo.setId(it.getId());
                    llmVo.setName(it.getName());
                    llmVo.setDefaultLLM(it.getDefaultLLM());
                    return llmVo;
                }).toList());
    }

    @EruptMenuAuth(AiConst.AI_CHAT)
    @GetMapping("/chats")
    public R<SimplePage<AiChat>> chats(@RequestParam("size") Integer size,
                                       @RequestParam("index") Integer index) {
        return R.ok(eruptDao.lambdaQuery(AiChat.class)
                .with(AiChat::getEruptUser).eq(EruptUserVo::getId, eruptUserService.getCurrentUid()).with()
                .orderByDesc(AiChat::getCreatedTime)
                .page(size, (index - 1) * size));
    }

    @EruptMenuAuth(AiConst.AI_CHAT)
    @GetMapping("/messages")
    public R<List<AiChatMessage>> messages(@RequestParam("chatId") Long chatId,
                                           @RequestParam("size") Integer size,
                                           @RequestParam("index") Integer index) {
        return R.ok(eruptDao.lambdaQuery(AiChatMessage.class)
                .eq(AiChatMessage::getChatId, chatId)
                .orderByDesc(AiChatMessage::getCreatedAt)
                .offset((index - 1) * size)
                .limit(size)
                .list());
    }

}
