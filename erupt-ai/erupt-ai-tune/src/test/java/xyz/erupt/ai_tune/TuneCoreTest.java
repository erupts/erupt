package xyz.erupt.ai_tune;

import org.junit.jupiter.api.Test;
import xyz.erupt.ai_tune.constants.TuneStatus;
import xyz.erupt.ai_tune.core.TuneCore;
import xyz.erupt.ai_tune.provider.ChatGptTune;
import xyz.erupt.ai_tune.provider.GLMTune;
import xyz.erupt.ai_tune.provider.QwenTune;
import xyz.erupt.ai_tune.provider.TogetherTune;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author YuePeng
 * date 2026/10/8
 */
public class TuneCoreTest {

    @Test
    public void registryMapsProviderCodes() {
        ChatGptTune openai = new ChatGptTune();
        GLMTune glm = new GLMTune();
        TogetherTune together = new TogetherTune();
        QwenTune qwen = new QwenTune();
        assertSame(openai, TuneCore.get("ChatGpt"));
        assertSame(openai, TuneCore.get("OpenAIAdapter"));
        assertSame(glm, TuneCore.get("GLM"));
        assertSame(together, TuneCore.get("Together"));
        assertSame(qwen, TuneCore.get("Qwen"));
        assertNull(TuneCore.get("Ollama"));
        assertNull(TuneCore.get(null));
        assertTrue(TuneCore.supportedCodes().contains("GLM"));
    }

    @Test
    public void statusSets() {
        assertTrue(TuneStatus.isTerminal(TuneStatus.SUCCEEDED));
        assertTrue(TuneStatus.isTerminal(TuneStatus.FAILED));
        assertTrue(TuneStatus.isTerminal(TuneStatus.CANCELLED));
        assertFalse(TuneStatus.isTerminal(TuneStatus.RUNNING));
        assertTrue(TuneStatus.STARTABLE.contains(TuneStatus.FAILED));
        assertFalse(TuneStatus.STARTABLE.contains(TuneStatus.RUNNING));
        assertTrue(TuneStatus.CANCELLABLE.contains(TuneStatus.QUEUED));
        assertFalse(TuneStatus.CANCELLABLE.contains(TuneStatus.UPLOADING));
        assertTrue(TuneStatus.ACTIVE.contains(TuneStatus.UPLOADING));
    }

}
