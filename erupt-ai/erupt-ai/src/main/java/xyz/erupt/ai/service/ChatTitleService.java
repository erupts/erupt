package xyz.erupt.ai.service;

import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.core.LlmRequest;
import xyz.erupt.ai.model.AiChat;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Names a chat after its first message. A chat is created with that message as its title, which
 * reads as a sentence rather than a name; this asks the model answering the chat for a short one
 * in the background, so the reply itself never waits for it. The call carries the system prompt
 * (which also settles the language) and the message, but no history, expert prompt or tools.
 */
@Service
@Slf4j
public class ChatTitleService {

    // The opening of a long message is enough to name it
    private static final int MAX_MESSAGE_LENGTH = 500;

    private static final int MAX_TITLE_LENGTH = 40;

    private static final String PROMPT = """
            Write a title for a conversation that starts with the message below.
            At most 6 words, or 15 characters for Chinese, Japanese or Korean.
            Reply with the title only: no quotes, no trailing punctuation, no explanation.

            Message:
            """;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * @param placeholder the title the chat was created with; the chat is renamed only while it
     *                    still carries it, so a name the user typed meanwhile is never overwritten
     * @return the new title, or null when the chat keeps the one it has (a failed call, an empty
     * answer, or renamed by the user)
     */
    @Async
    public CompletableFuture<String> generate(MetaContext metaContext, LLM llm, Long chatId, String placeholder, String firstMessage) {
        String title = null;
        try {
            MetaContext.set(metaContext);
            // One transaction around the call: the system prompt is assembled while the model is
            // asked and reads the user's roles, a lazy collection that needs an open session
            title = transactionTemplate.execute(status -> {
                LlmRequest request = llm.toLlmRequest();
                request.setThinking(false);
                String candidate = clean(LlmCore.getLLM(llm.getLlm()).chat(request,
                        List.of(UserMessage.from(PROMPT + StringUtils.left(firstMessage, MAX_MESSAGE_LENGTH)))));
                AiChat chat = null == candidate ? null : eruptDao.find(AiChat.class, chatId);
                if (null == chat || !placeholder.equals(chat.getTitle())) return null;
                chat.setTitle(candidate);
                return candidate;
            });
        } catch (Exception e) {
            log.warn("Chat title generation failed chatId: {}", chatId, e);
        } finally {
            MetaContext.remove();
        }
        return CompletableFuture.completedFuture(title);
    }

    // Models decorate a title however they are asked not to: keep the first line, drop the
    // wrapping quotes, markdown marks and closing punctuation
    public static String clean(String raw) {
        if (null == raw) return null;
        String title = raw.strip().lines().findFirst().orElse("")
                .replaceAll("^[\\s\"'`*#\\p{Pi}\\p{Ps}]+", "")
                .replaceAll("[\\s\"'`*\\p{Pf}\\p{Pe}\\p{Po}]+$", "");
        return title.isEmpty() ? null : StringUtils.left(title, MAX_TITLE_LENGTH);
    }

}
