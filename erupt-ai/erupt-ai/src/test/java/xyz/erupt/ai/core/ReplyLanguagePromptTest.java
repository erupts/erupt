package xyz.erupt.ai.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import xyz.erupt.core.context.MetaContext;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The reply language follows the console the request came from. Without this the model
 * answers in whatever language it leans towards, so an English console gets Chinese back.
 *
 * @author YuePeng
 */
public class ReplyLanguagePromptTest {

    @AfterEach
    public void clear() {
        MetaContext.remove();
    }

    @Test
    public void namesTheRequestLanguage() {
        MetaContext.registerLang("zh-CN");
        String prompt = LlmCore.languagePrompt();
        assertTrue(prompt.contains("Chinese"), prompt);
        assertTrue(prompt.contains("zh-CN"), prompt);
    }

    @Test
    public void everyConsoleLanguageIsNamed() {
        for (String lang : new String[]{"en-US", "zh-TW", "ja-JP", "ko-KR", "fr-FR", "de-DE",
                "es-ES", "pt-PT", "ru-RU", "id-ID", "ar-SA"}) {
            MetaContext.registerLang(lang);
            String prompt = LlmCore.languagePrompt();
            assertTrue(prompt.contains(lang), lang + " tag missing from: " + prompt);
        }
    }

    // A tag no Locale can read must not produce a half-written instruction; the model is
    // better off with no language rule than with a nonsensical one
    @Test
    public void unreadableTagAddsNothing() {
        MetaContext.registerLang("!!!");
        assertNull(LlmCore.languagePrompt());
    }

}
