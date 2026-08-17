package xyz.erupt.ai.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.ai.constants.AiConst;
import xyz.erupt.ai.model.AiChat;
import xyz.erupt.ai.model.AiChatMessage;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.prompt.SystemPromptProvider;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUserVo;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2026/7/29
 */
@AiToolbox
@Component
public class AiChatTools implements SystemPromptProvider {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final int PREVIEW_LENGTH = 100;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @PostConstruct
    public void init() {
        SystemPromptProvider.registerProvider(this);
    }

    @Override
    public String getPrompt() {
        return """
                ## Chat Sessions
                Call `getCurrentChat` when you need the ID or title of the ongoing conversation
                (e.g. the user says "this chat", "rename this session").
                When the user refers to a previous conversation ("as we discussed before", "last time"),
                call `searchChatHistory` or `listChats` to locate it, then `getChatMessages` to read the details.
                Use `renameChat` / `deleteChat` only when the user explicitly asks to rename or delete a session.
                """;
    }

    @Tool("Get the ID and title of the current chat session (the conversation this message belongs to).")
    public String getCurrentChat() {
        Object chatId = MetaContext.getVars().get(AiConst.VAR_CHAT_ID);
        if (null == chatId) return "No active chat session in the current context.";
        AiChat chat = findUserChat(((Number) chatId).longValue());
        if (null == chat) return "Current chat session ID: " + chatId;
        return "Current chat session ID: " + chat.getId() + ", title: " + chat.getTitle();
    }

    @Tool("List the current user's recent chat sessions with ID, title and created time, newest first.")
    public String listChats(@P("Max number of sessions to return, e.g. 20") Integer limit) {
        List<AiChat> chats = eruptDao.lambdaQuery(AiChat.class)
                .with(AiChat::getEruptUser).eq(EruptUserVo::getId, MetaContext.getUser().getUid()).with()
                .orderByDesc(AiChat::getCreatedTime)
                .limit(null == limit || limit <= 0 ? 20 : limit)
                .list();
        if (chats.isEmpty()) return "No chat sessions yet.";
        return chats.stream()
                .map(c -> c.getId() + ": " + c.getTitle() + " (" + c.getCreatedTime().format(TIME_FORMAT) + ")")
                .collect(Collectors.joining("\n"));
    }

    @Tool("Search the current user's chat history by keyword across all sessions. " +
            "Returns matched messages with session ID, sender, time and a content preview.")
    public String searchChatHistory(@P("Keyword to search in message content") String keyword,
                                    @P("Max number of messages to return, e.g. 10") Integer limit) {
        List<Long> chatIds = userChatIds();
        if (chatIds.isEmpty()) return "No chat sessions yet.";
        List<AiChatMessage> messages = eruptDao.lambdaQuery(AiChatMessage.class)
                .in(AiChatMessage::getChatId, chatIds)
                .like(AiChatMessage::getContent, keyword)
                .orderByDesc(AiChatMessage::getCreatedAt)
                .limit(null == limit || limit <= 0 ? 10 : limit)
                .list();
        if (messages.isEmpty()) return "No messages found for keyword: " + keyword;
        return messages.stream().map(this::renderMessage).collect(Collectors.joining("\n"));
    }

    @Tool("Read the recent messages of a chat session by session ID, newest first.")
    public String getChatMessages(@P("Chat session ID") Long chatId,
                                  @P("Max number of messages to return, e.g. 20") Integer limit) {
        AiChat chat = findUserChat(chatId);
        if (null == chat) return "Chat session not found: " + chatId;
        List<AiChatMessage> messages = eruptDao.lambdaQuery(AiChatMessage.class)
                .eq(AiChatMessage::getChatId, chatId)
                .orderByDesc(AiChatMessage::getCreatedAt)
                .limit(null == limit || limit <= 0 ? 20 : limit)
                .list();
        if (messages.isEmpty()) return "No messages in chat session: " + chatId;
        return "Session: " + chat.getTitle() + "\n" +
                messages.stream().map(this::renderMessage).collect(Collectors.joining("\n"));
    }

    @Tool("Rename a chat session. Only call when the user explicitly asks to rename it.")
    public String renameChat(@P("Chat session ID") Long chatId, @P("New title") String title) {
        AiChat chat = findUserChat(chatId);
        if (null == chat) return "Chat session not found: " + chatId;
        chat.setTitle(title.length() > 100 ? title.substring(0, 100) : title);
        transactionTemplate.executeWithoutResult(s -> eruptDao.merge(chat));
        return "Chat session renamed: " + chatId;
    }

    @Tool("Delete a chat session. Only call when the user explicitly asks to delete it.")
    public String deleteChat(@P("Chat session ID to delete") Long chatId) {
        AiChat chat = findUserChat(chatId);
        if (null == chat) return "Chat session not found: " + chatId;
        chat.setDeleted(true);
        transactionTemplate.executeWithoutResult(s -> eruptDao.merge(chat));
        return "Chat session deleted: " + chatId;
    }

    // Ownership guard: only the current user's sessions are visible to the model
    private AiChat findUserChat(Long chatId) {
        return eruptDao.lambdaQuery(AiChat.class)
                .eq(AiChat::getId, chatId)
                .with(AiChat::getEruptUser).eq(EruptUserVo::getId, MetaContext.getUser().getUid()).with()
                .one();
    }

    private List<Long> userChatIds() {
        return eruptDao.lambdaQuery(AiChat.class)
                .with(AiChat::getEruptUser).eq(EruptUserVo::getId, MetaContext.getUser().getUid()).with()
                .list().stream().map(AiChat::getId).collect(Collectors.toList());
    }

    private String renderMessage(AiChatMessage message) {
        String content = message.getContent();
        if (null != content && content.length() > PREVIEW_LENGTH) {
            content = content.substring(0, PREVIEW_LENGTH) + "...";
        }
        return "[chat " + message.getChatId() + "] " + message.getSenderType() + " @ "
                + message.getCreatedAt().format(TIME_FORMAT) + ": " + content;
    }

}
