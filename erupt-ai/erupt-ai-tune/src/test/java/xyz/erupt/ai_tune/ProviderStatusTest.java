package xyz.erupt.ai_tune;

import org.junit.jupiter.api.Test;
import xyz.erupt.ai_tune.constants.TuneStatus;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Status vocabularies differ per provider; every one must land in erupt's set.
 *
 * @author YuePeng
 * date 2026/10/8
 */
public class ProviderStatusTest {

    private static String map(String className, String status) throws Exception {
        Method m = Class.forName("xyz.erupt.ai_tune.provider." + className).getDeclaredMethod("mapStatus", String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, status);
    }

    @Test
    public void openAi() throws Exception {
        assertEquals(TuneStatus.VALIDATING, map("OpenAITune", "validating_files"));
        assertEquals(TuneStatus.QUEUED, map("OpenAITune", "queued"));
        assertEquals(TuneStatus.RUNNING, map("OpenAITune", "running"));
        assertEquals(TuneStatus.SUCCEEDED, map("OpenAITune", "succeeded"));
        assertEquals(TuneStatus.FAILED, map("OpenAITune", "failed"));
        assertEquals(TuneStatus.CANCELLED, map("OpenAITune", "cancelled"));
        assertEquals(TuneStatus.QUEUED, map("OpenAITune", null));
    }

    @Test
    public void together() throws Exception {
        assertEquals(TuneStatus.QUEUED, map("TogetherTune", "pending"));
        assertEquals(TuneStatus.RUNNING, map("TogetherTune", "compressing"));
        assertEquals(TuneStatus.SUCCEEDED, map("TogetherTune", "completed"));
        assertEquals(TuneStatus.FAILED, map("TogetherTune", "error"));
        assertEquals(TuneStatus.CANCELLED, map("TogetherTune", "cancelled"));
    }

    @Test
    public void qwen() throws Exception {
        assertEquals(TuneStatus.QUEUED, map("QwenTune", "PENDING"));
        assertEquals(TuneStatus.RUNNING, map("QwenTune", "RUNNING"));
        assertEquals(TuneStatus.SUCCEEDED, map("QwenTune", "SUCCEEDED"));
        assertEquals(TuneStatus.FAILED, map("QwenTune", "FAILED"));
        assertEquals(TuneStatus.CANCELLED, map("QwenTune", "CANCELED"));
    }

}
