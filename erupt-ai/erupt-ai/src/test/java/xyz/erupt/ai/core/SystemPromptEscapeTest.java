package xyz.erupt.ai.core;

import dev.langchain4j.model.input.PromptTemplate;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AiServices treats the system message as a prompt template, so an unknown
 * "{{name}}" aborts the call with "Value for the variable 'name' is missing".
 * The system message is assembled from free text nobody screens: the configured
 * system prompt, role and agent prompts stored in the database, and module
 * prompts that embed HTML templates. Every one of them must survive.
 *
 * @author YuePeng
 */
public class SystemPromptEscapeTest {

    @Test
    public void openingBracePairIsBroken() {
        // The exact shape that took AI canvas generation down: mustache syntax in prose
        assertEquals("write { { interpolation }} in the DOM",
                LlmCore.escapeTemplateVars("write {{ interpolation }} in the DOM"));
    }

    @Test
    public void everyOccurrenceIsHandled() {
        assertEquals("{ {a}} and { {b}} and { {c}}",
                LlmCore.escapeTemplateVars("{{a}} and {{b}} and {{c}}"));
    }

    @Test
    public void vueTemplatesEmbeddedInPromptsSurvive() {
        String demo = "<span>{{ row.name }}</span><b>{{total}}</b>";
        String escaped = LlmCore.escapeTemplateVars(demo);
        assertFalse(escaped.contains("{{"), escaped);
        // Still readable to the model, only the brace pair is split
        assertTrue(escaped.contains("row.name"));
        assertTrue(escaped.contains("total"));
    }

    @Test
    public void ordinaryPromptsAreUntouched() {
        String plain = "Return JSON like {\"a\": 1} and use ${base} for asset URLs.";
        assertEquals(plain, LlmCore.escapeTemplateVars(plain));
        assertEquals("", LlmCore.escapeTemplateVars(""));
        assertNull(LlmCore.escapeTemplateVars(null));
    }

    /** Proves the escape against the real template engine, not just the replacement */
    @Test
    public void langchain4jRejectsRawMustacheAndAcceptsTheEscapedForm() {
        String raw = "NEVER write {{ interpolation }} in the DOM";
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> PromptTemplate.from(raw).apply(Map.of("it", "x")));
        assertTrue(e.getMessage().contains("interpolation"), e.getMessage());

        String escaped = LlmCore.escapeTemplateVars(raw);
        assertDoesNotThrow(() -> PromptTemplate.from(escaped).apply(Map.of("it", "x")));
    }

    @Test
    public void singleBracesAndCssBlocksAreUntouched() {
        String css = "body { margin: 0; } .x { padding: 4px; }";
        assertEquals(css, LlmCore.escapeTemplateVars(css));
    }

}
