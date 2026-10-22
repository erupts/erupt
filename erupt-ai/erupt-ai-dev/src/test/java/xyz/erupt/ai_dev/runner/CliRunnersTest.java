package xyz.erupt.ai_dev.runner;

import org.junit.jupiter.api.Test;
import xyz.erupt.ai_dev.constant.EventKind;
import xyz.erupt.ai_dev.prop.EruptAiDevProp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CliRunnersTest {

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

    private static final CodingAgentRunner.Spec SPEC = new CodingAgentRunner.Spec(Path.of("/w"), "do it", "gpt-5", null,
            Path.of("/run/mcp.json"), "http://127.0.0.1:8080/mcp", "secret-token");

    private static <T extends AbstractCliRunner> T withProp(T runner) {
        runner.eruptAiDevProp = new EruptAiDevProp();
        return runner;
    }

    @Test
    void openCodeLinesAsPrinted() {
        // recorded from opencode v2 run --format json
        OpenCodeRunner runner = withProp(new OpenCodeRunner());
        runner.onLine("{\"type\":\"step_start\",\"timestamp\":1,\"sessionID\":\"ses_1\",\"part\":{\"type\":\"step-start\"}}", listener);
        runner.onLine("{\"type\":\"text\",\"timestamp\":2,\"sessionID\":\"ses_1\",\"part\":{\"type\":\"text\",\"text\":\"PONG\"}}", listener);
        runner.onLine("{\"type\":\"error\",\"sessionID\":\"ses_1\",\"error\":{\"type\":\"provider.internal\",\"message\":\"Endpoint is unavailable.\"}}", listener);
        assertEquals("ses_1", session);
        assertEquals(List.of("TEXT|null|PONG", "ERROR|null|Endpoint is unavailable."), seen);
        assertNull(result, "no closing event: the exit code decides");
        Map<String, String> env = runner.environment(SPEC);
        assertTrue(env.get("OPENCODE_CONFIG_CONTENT").contains("\"Authorization\":\"Bearer secret-token\""));
        assertEquals(List.of("opencode", "run", "--format", "json", "--standalone", "--auto", "-m", "gpt-5"), runner.command(SPEC));
    }

    @Test
    void codexEventsAndTheTokenStaysOffTheCommandLine() {
        CodexRunner runner = withProp(new CodexRunner());
        runner.onLine("{\"type\":\"thread.started\",\"thread_id\":\"th_9\"}", listener);
        runner.onLine("{\"type\":\"item.completed\",\"item\":{\"type\":\"command_execution\",\"command\":\"ls\",\"aggregated_output\":\"a.txt\"}}", listener);
        runner.onLine("{\"type\":\"item.completed\",\"item\":{\"type\":\"agent_message\",\"text\":\"done\"}}", listener);
        runner.onLine("{\"type\":\"turn.completed\",\"usage\":{\"input_tokens\":10,\"cached_input_tokens\":5,\"output_tokens\":3}}", listener);
        assertEquals("th_9", session);
        assertEquals(List.of("TOOL|shell|ls", "TOOL_RESULT|null|a.txt", "TEXT|null|done"), seen);
        assertEquals(true, result[0]);
        assertEquals(18L, result[3]);
        List<String> command = runner.command(SPEC);
        assertEquals("-", command.get(command.size() - 1), "the prompt comes from stdin");
        assertTrue(command.contains("mcp_servers.erupt.url=\"http://127.0.0.1:8080/mcp\""));
        assertTrue(command.stream().noneMatch(it -> it.contains("secret-token")));
        assertEquals("secret-token", runner.environment(SPEC).get("ERUPT_MCP_TOKEN"));
    }

    @Test
    void kimiMessages() {
        KimiRunner runner = withProp(new KimiRunner());
        runner.onLine("{\"role\":\"assistant\",\"content\":[{\"type\":\"text\",\"text\":\"looking\"}],\"tool_calls\":[{\"function\":{\"name\":\"Shell\",\"arguments\":\"{\\\"command\\\":\\\"ls\\\"}\"}}]}", listener);
        runner.onLine("{\"role\":\"tool\",\"content\":\"a.txt\"}", listener);
        assertEquals(List.of("TEXT|null|looking", "TOOL|Shell|{\"command\":\"ls\"}", "TOOL_RESULT|null|a.txt"), seen);
        assertTrue(runner.command(SPEC).containsAll(List.of("--mcp-config-file", "/run/mcp.json")));
    }

    @Test
    void plainCliTakesItsPromptWhereConfigured() {
        EruptAiDevProp.Cli cli = new EruptAiDevProp.Cli();
        cli.setBinary("zcode");
        cli.setArgs(List.of("-p"));
        PlainCliRunner stdin = new PlainCliRunner("zcode", cli);
        assertTrue(stdin.promptOnStdin());
        assertEquals(List.of("zcode", "-p"), stdin.command(SPEC));
        cli.setPromptAsArgument(true);
        assertEquals(List.of("zcode", "-p", "do it"), stdin.command(SPEC));
        stdin.onLine("VERDICT: PASS", listener);
        assertEquals(List.of("TEXT|null|VERDICT: PASS"), seen);
    }

    @Test
    void whichFindsExecutablesOnThePath() {
        assertNotNull(DevRunners.which("sh"));
        assertNull(DevRunners.which("no-such-cli-" + System.nanoTime()));
        assertNull(DevRunners.which("/etc/hosts"), "a file that is not executable");
    }

    @Test
    void cursorToolCallsAndThePromptAsLastArgument() {
        CursorRunner runner = withProp(new CursorRunner());
        runner.onLine("{\"type\":\"system\",\"subtype\":\"init\",\"session_id\":\"chat-7\",\"model\":\"gpt-5\"}", listener);
        runner.onLine("{\"type\":\"tool_call\",\"subtype\":\"started\",\"call_id\":\"c1\",\"tool_call\":{\"readToolCall\":{\"args\":{\"path\":\"a.txt\"}}}}", listener);
        runner.onLine("{\"type\":\"tool_call\",\"subtype\":\"completed\",\"call_id\":\"c1\",\"tool_call\":{\"readToolCall\":{\"result\":{\"success\":{\"content\":\"hi\"}}}}}", listener);
        runner.onLine("{\"type\":\"result\",\"subtype\":\"success\",\"is_error\":false,\"result\":\"done\",\"session_id\":\"chat-7\"}", listener);
        assertEquals("chat-7", session);
        assertEquals("TOOL|read|{\"path\":\"a.txt\"}", seen.get(1));
        assertTrue(seen.get(2).startsWith("TOOL_RESULT|read|"));
        assertEquals(true, result[0]);
        assertEquals("done", result[1]);
        List<String> command = runner.command(SPEC);
        assertFalse(runner.promptOnStdin());
        assertEquals("do it", command.get(command.size() - 1));
        assertTrue(command.containsAll(List.of("-p", "--output-format", "stream-json", "--force")));
    }

    @Test
    void qwenSpeaksTheSameStream() {
        QwenCodeRunner qwen = withProp(new QwenCodeRunner());
        qwen.onLine("{\"type\":\"system\",\"subtype\":\"session_start\",\"session_id\":\"q-1\"}", listener);
        qwen.onLine("{\"type\":\"assistant\",\"message\":{\"content\":[{\"type\":\"text\",\"text\":\"ok\"}]}}", listener);
        assertEquals("q-1", session);
        assertEquals(List.of("qwen", "--output-format", "stream-json", "--yolo", "-m", "gpt-5"), qwen.command(SPEC));
    }

}
