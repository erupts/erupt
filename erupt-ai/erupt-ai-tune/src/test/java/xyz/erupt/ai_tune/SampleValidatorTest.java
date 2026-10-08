package xyz.erupt.ai_tune;

import org.junit.jupiter.api.Test;
import xyz.erupt.ai_tune.constants.DatasetFormat;
import xyz.erupt.ai_tune.service.SampleValidator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author YuePeng
 * date 2026/10/8
 */
public class SampleValidatorTest {

    @Test
    public void chatSampleAccepted() {
        SampleValidator.Result r = SampleValidator.validate(DatasetFormat.CHAT,
                "{\"messages\":[{\"role\":\"system\",\"content\":\"Be brief\"},{\"role\":\"user\",\"content\":\"Hi\"},{\"role\":\"assistant\",\"content\":\"Hello there\"}]}");
        assertTrue(r.isValid(), r.getError());
        assertEquals(3, r.getTurns());
        assertTrue(r.getTokens() > 0);
    }

    @Test
    public void chatSampleMustEndWithAssistant() {
        SampleValidator.Result r = SampleValidator.validate(DatasetFormat.CHAT,
                "{\"messages\":[{\"role\":\"assistant\",\"content\":\"Hello\"},{\"role\":\"user\",\"content\":\"Hi\"}]}");
        assertFalse(r.isValid());
        assertTrue(r.getError().contains("assistant"));
    }

    @Test
    public void assistantToolCallWithoutContentAccepted() {
        SampleValidator.Result r = SampleValidator.validate(DatasetFormat.CHAT,
                "{\"messages\":[{\"role\":\"user\",\"content\":\"Weather?\"},"
                        + "{\"role\":\"assistant\",\"tool_calls\":[{\"id\":\"1\",\"type\":\"function\",\"function\":{\"name\":\"weather\",\"arguments\":\"{}\"}}]},"
                        + "{\"role\":\"tool\",\"tool_call_id\":\"1\",\"content\":\"sunny\"},{\"role\":\"assistant\",\"content\":\"Sunny\"}]}");
        assertTrue(r.isValid(), r.getError());
    }

    @Test
    public void rejectsBrokenJsonAndUnknownRole() {
        assertFalse(SampleValidator.validate(DatasetFormat.CHAT, "{not json").isValid());
        assertFalse(SampleValidator.validate(DatasetFormat.CHAT, "[1,2]").isValid());
        SampleValidator.Result r = SampleValidator.validate(DatasetFormat.CHAT,
                "{\"messages\":[{\"role\":\"human\",\"content\":\"Hi\"},{\"role\":\"assistant\",\"content\":\"Hello\"}]}");
        assertFalse(r.isValid());
        assertTrue(r.getError().contains("human"));
    }

    @Test
    public void preferenceSample() {
        String ok = "{\"input\":{\"messages\":[{\"role\":\"user\",\"content\":\"Pick\"}]},"
                + "\"preferred_output\":[{\"role\":\"assistant\",\"content\":\"A\"}],"
                + "\"non_preferred_output\":[{\"role\":\"assistant\",\"content\":\"B\"}]}";
        SampleValidator.Result r = SampleValidator.validate(DatasetFormat.PREFERENCE, ok);
        assertTrue(r.isValid(), r.getError());
        assertEquals(2, r.getTurns());
        // The same line is not a chat sample
        assertFalse(SampleValidator.validate(DatasetFormat.CHAT, ok).isValid());
        // Preferred output must come from the assistant
        String wrongRole = ok.replace("\"preferred_output\":[{\"role\":\"assistant\"", "\"preferred_output\":[{\"role\":\"user\"");
        assertFalse(SampleValidator.validate(DatasetFormat.PREFERENCE, wrongRole).isValid());
    }

    @Test
    public void tokenEstimateTreatsCjkPerCharacter() {
        int latin = SampleValidator.estimateTokens("hello world this is a test");
        assertEquals(6, latin);
        // Six Han characters (U+4E2D U+6587 repeated) count one token each
        String han = "\u4e2d\u6587\u4e2d\u6587\u4e2d\u6587";
        assertEquals(6, SampleValidator.estimateTokens(han));
        assertEquals(0, SampleValidator.estimateTokens("   "));
    }

}
