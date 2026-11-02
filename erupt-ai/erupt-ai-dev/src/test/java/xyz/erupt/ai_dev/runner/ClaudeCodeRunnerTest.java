package xyz.erupt.ai_dev.runner;

import org.junit.jupiter.api.Test;
import xyz.erupt.ai_dev.constant.EventKind;
import xyz.erupt.ai_dev.service.DevPrompts;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Claude Code's stream-json, as printed by {@code claude -p --output-format stream-json --verbose}.
 *
 * @author YuePeng
 */
class ClaudeCodeRunnerTest {

    private final List<String> seen = new ArrayList<>();

    private String session;

    private Object[] result;

    private final CodingAgentRunner.Listener listener = new CodingAgentRunner.Listener() {
        @Override
        public void session(String sessionId) {
            session = sessionId;
        }

        @Override
        public void event(EventKind kind, String name, String content) {
            seen.add(kind + "|" + name + "|" + content);
        }

        @Override
        public void result(boolean success, String text, Double costUsd, Long tokens) {
            result = new Object[]{success, text, costUsd, tokens};
        }
    };

    @Test
    void readsAWholeSession() {
        ClaudeCodeRunner runner = new ClaudeCodeRunner();
        for (String line : List.of(
                "{\"type\":\"system\",\"subtype\":\"init\",\"session_id\":\"abc-123\",\"model\":\"claude-opus-5-5\",\"tools\":[\"Bash\"]}",
                "{\"type\":\"assistant\",\"message\":{\"content\":[{\"type\":\"text\",\"text\":\"PROGRESS 20 design\"},"
                        + "{\"type\":\"tool_use\",\"id\":\"t1\",\"name\":\"Bash\",\"input\":{\"command\":\"ls\"}}]}}",
                "{\"type\":\"user\",\"message\":{\"content\":[{\"type\":\"tool_result\",\"tool_use_id\":\"t1\",\"content\":[{\"type\":\"text\",\"text\":\"web\\napi\"}]}]}}",
                "not json: a stray line the CLI printed",
                "",
                "{\"type\":\"result\",\"subtype\":\"success\",\"is_error\":false,\"result\":\"All done\",\"session_id\":\"abc-123\","
                        + "\"total_cost_usd\":0.42,\"usage\":{\"input_tokens\":100,\"output_tokens\":20,\"cache_read_input_tokens\":5}}")) {
            runner.onLine(line, listener);
        }
        assertEquals("abc-123", session);
        assertEquals(List.of(
                "SYSTEM|init|claude-opus-5-5",
                "TEXT|null|PROGRESS 20 design",
                "TOOL|Bash|{\"command\":\"ls\"}",
                "TOOL_RESULT|null|web\napi"), seen);
        assertArrayEquals(new Object[]{true, "All done", 0.42, 125L}, result);
    }

    @Test
    void anErrorResultIsAFailure() {
        new ClaudeCodeRunner().onLine("{\"type\":\"result\",\"subtype\":\"error_max_turns\",\"is_error\":true}", listener);
        assertEquals(false, result[0]);
    }

    @Test
    void refusesOptionLikeArguments() {
        assertThrows(IllegalArgumentException.class, () -> CodingAgentRunner.safeArg("--dangerously-skip-permissions"));
        assertThrows(IllegalArgumentException.class, () -> CodingAgentRunner.safeArg("a b"));
        assertEquals("claude-opus-5-5", CodingAgentRunner.safeArg("claude-opus-5-5"));
    }

    @Test
    void readsMarkerLines() {
        assertEquals("FAIL export ignores the filter", DevPrompts.verdict("checked\nVERDICT: PASS\nre-checked\nVERDICT: FAIL export ignores the filter"));
        assertEquals("PASS", DevPrompts.verdict("all good\nVERDICT: PASS"));
        assertNull(DevPrompts.verdict("no verdict here"));
        Matcher m = DevPrompts.PROGRESS.matcher("thinking\nPROGRESS 40 implement\n");
        assertTrue(m.find());
        assertEquals("40", m.group(1));
        assertEquals("implement", m.group(2).trim());
    }

}
