package xyz.erupt.ai;

import org.junit.jupiter.api.Test;
import xyz.erupt.ai.core.LlmCore;
import xyz.erupt.ai.llm.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Registry contract for the built-in LLM adapters. Instantiating an adapter
 * must self-register it under its code(), and every adapter must expose
 * complete, network-free form metadata. Mirrors {@link EmbeddingCoreTest}.
 *
 * @author YuePeng
 */
public class LlmCoreRegistryTest {

    // Instantiating self-registers into the LlmCore registry (constructor does llms.put(code(), this))
    private static final List<LlmCore> ADAPTERS = List.of(
            new ChatGpt(), new Claude(), new DeepSeek(), new Doubao(), new Fireworks(),
            new GLM(), new Gemini(), new Grok(), new Mimo(), new MiniMax(),
            new Mistral(), new Moonshot(), new Ollama(), new OpenAIAdapter(), new OpenRouter(),
            new OrcaRouter(), new Qwen(), new Requesty(), new Together()
    );

    @Test
    public void selfRegistersUnderCode() {
        for (LlmCore adapter : ADAPTERS) {
            assertSame(adapter, LlmCore.getLLM(adapter.code()),
                    adapter.getClass().getSimpleName() + " must be retrievable by its code");
        }
    }

    @Test
    public void exposesCompleteMetadata() {
        for (LlmCore adapter : ADAPTERS) {
            String who = adapter.getClass().getSimpleName();
            assertNotNull(adapter.code(), who + " code");
            assertFalse(adapter.code().isBlank(), who + " code must not be blank");
            // model() may be an empty template (e.g. the generic OpenAI adapter) but never null
            assertNotNull(adapter.model(), who + " model");
            assertNotNull(adapter.api(), who + " api");
            assertTrue(adapter.api().startsWith("http"), who + " api should be a URL: " + adapter.api());
        }
    }

    @Test
    public void adapterCodesAreUnique() {
        Set<String> codes = new HashSet<>();
        for (LlmCore adapter : ADAPTERS) {
            assertTrue(codes.add(adapter.code()), "duplicate adapter code: " + adapter.code());
        }
    }

}
