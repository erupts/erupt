package xyz.erupt.test.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.ai.model.AiChat;
import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.service.ChatTitleService;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A chat is created with its first message as the title and renamed in the background with what the
 * model makes of that message — through the real call path, system prompt included, on a thread of
 * its own — unless the user has named the chat meanwhile or the model has nothing usable to say.
 */
class ChatTitleTest extends EruptApplicationTests {

    private static final String FIRST_MESSAGE = "Could you walk me through how the expense claim approval flow works?";

    @Resource
    private ChatTitleService chatTitleService;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private TransactionTemplate transactionTemplate;

    private Long chatId;

    @BeforeEach
    void setUp() {
        MetaContext.remove();
        EruptUser admin = eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, eruptUpmsProp.getDefaultAccount()).one();
        MetaContext.register(new MetaUser(admin.getId(), admin.getAccount(), admin.getName(), null));
        chatId = transactionTemplate.execute(status -> {
            AiChat chat = new AiChat();
            chat.setTitle(FIRST_MESSAGE);
            chat.setCreatedTime(LocalDateTime.now());
            chat.setEruptUser(new EruptUserVo(admin.getId()));
            eruptDao.persist(chat);
            return chat.getId();
        });
    }

    @AfterEach
    void cleanUp() {
        MetaContext.remove();
    }

    @Test
    void renamesChatWithTheModelsAnswer() throws Exception {
        StandInLlm.answer = "\"Expense claim approval flow.\"";
        assertEquals("Expense claim approval flow", generate());
        assertEquals("Expense claim approval flow", title());
    }

    @Test
    void keepsANameTheUserGaveMeanwhile() throws Exception {
        transactionTemplate.executeWithoutResult(status -> eruptDao.find(AiChat.class, chatId).setTitle("My own name"));
        StandInLlm.answer = "Expense claim approval flow";
        assertNull(generate());
        assertEquals("My own name", title());
    }

    @Test
    void keepsTheTitleWhenTheModelSaysNothing() throws Exception {
        StandInLlm.answer = " \"\" ";
        assertNull(generate());
        assertEquals(FIRST_MESSAGE, title());
    }

    @Test
    void stripsDecoration() {
        assertEquals("Expense claim flow", ChatTitleService.clean("## **Expense claim flow**"));
        assertEquals("Expense claim flow", ChatTitleService.clean("《Expense claim flow》。"));
        assertEquals("C++ vs. Rust", ChatTitleService.clean("C++ vs. Rust"));
        assertEquals("Expense claim flow", ChatTitleService.clean("Expense claim flow\n\nThis title sums up the request."));
        assertEquals(40, ChatTitleService.clean("x".repeat(200)).length());
        assertNull(ChatTitleService.clean(null));
    }

    private String generate() throws Exception {
        LLM llm = new LLM();
        llm.setLlm(StandInLlm.CODE);
        llm.setModel("stand-in");
        llm.setConfig("{}");
        return chatTitleService.generate(MetaContext.get(), llm, chatId, FIRST_MESSAGE, FIRST_MESSAGE).get(30, TimeUnit.SECONDS);
    }

    private String title() {
        return transactionTemplate.execute(status -> eruptDao.find(AiChat.class, chatId).getTitle());
    }

}
