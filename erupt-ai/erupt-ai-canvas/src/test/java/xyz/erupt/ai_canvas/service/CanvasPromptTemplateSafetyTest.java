package xyz.erupt.ai_canvas.service;

import org.junit.jupiter.api.Test;
import xyz.erupt.ai_canvas.fun.EruptCanvasModelProvider;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Everything this module contributes to the system message is run through a
 * prompt template by AiServices, where "{{name}}" is a variable and an unknown
 * one aborts generation. LlmCore neutralizes the sequence as a last resort, but
 * that mangles the text the model reads, so the prompts we author must not rely
 * on it. Teach mustache syntax in words instead of writing the braces.
 *
 * @author YuePeng
 */
class CanvasPromptTemplateSafetyTest {

    private static final String OPENING = "{" + "{";

    private static String resource(String path) throws Exception {
        try (InputStream is = AiCanvasService.class.getResourceAsStream(path)) {
            return new String(Objects.requireNonNull(is, path).readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void skillPromptCarriesNoTemplateVariables() throws Exception {
        String skill = resource("/prompts/ai-canvas-skill.md");
        assertFalse(skill.contains(OPENING), "ai-canvas-skill.md must not contain mustache braces");
    }

    @Test
    void providerGuidesCarryNoTemplateVariables() {
        EruptCanvasModelProvider provider = new EruptCanvasModelProvider();
        assertFalse(provider.queryGuide().contains(OPENING), "queryGuide must not contain mustache braces");
        assertFalse(Objects.requireNonNull(provider.writeGuide()).contains(OPENING),
                "writeGuide must not contain mustache braces");
    }

}
