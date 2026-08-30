package xyz.erupt.ai;

import org.junit.jupiter.api.Test;
import xyz.erupt.ai.constants.ResponseFormat;
import xyz.erupt.ai.core.LlmRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Locks the default contract of {@link LlmRequest} for programmatic callers
 * (canvas, staff, notice...): a bare request must NOT expose the global toolbox
 * nor inherit the SystemPromptProvider prompts that advertise those tools —
 * otherwise the model calls tools that do not exist in that conversation.
 * Only erupt-ai's own chat entry opts in explicitly.
 */
public class LlmRequestDefaultsTest {

    @Test
    public void programmaticCallersDoNotInheritToolboxSurface() {
        LlmRequest request = new LlmRequest();
        assertEquals(Boolean.FALSE, request.getAutoCallTool(),
                "autoCallTool must default to false: the global toolbox/MCP tools are opt-in");
        assertEquals(Boolean.FALSE, request.getSystemPromptProviders(),
                "systemPromptProviders must default to false: toolbox usage prompts are opt-in");
    }

    @Test
    public void otherDefaults() {
        LlmRequest request = new LlmRequest();
        assertEquals(Boolean.TRUE, request.getStrictTools());
        assertFalse(request.isThinking());
        assertEquals(ResponseFormat.text, request.getResponseFormat());
        assertNull(request.getTools(), "request-scoped tools default to none");
    }

}
