package xyz.erupt.test.aidev;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.web.server.LocalServerPort;
import xyz.erupt.ai_dev.constant.AgentRole;
import xyz.erupt.ai_dev.constant.EventKind;
import xyz.erupt.ai_dev.mcp.DevMcpGrants;
import xyz.erupt.ai_dev.runner.ClaudeCodeRunner;
import xyz.erupt.ai_dev.runner.CodingAgentRunner;
import xyz.erupt.ai_dev.runner.DevAgentProcess;
import xyz.erupt.test.EruptApplicationTests;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The real Claude Code CLI against erupt's MCP endpoint. It calls a model and costs tokens, so it runs only
 * when asked: {@code mvn -pl erupt-test test -Dtest=AiDevRealClaudeMcpTest -Daidev.realClaude=true}.
 */
@EnabledIfSystemProperty(named = "aidev.realClaude", matches = "true")
class AiDevRealClaudeMcpTest extends EruptApplicationTests {

    @LocalServerPort
    private int port;

    @Resource
    private DevMcpGrants devMcpGrants;

    @Resource
    private ClaudeCodeRunner claudeCodeRunner;

    @Resource
    private DevAgentProcess devAgentProcess;

    @Test
    void claudeCodeCallsTheTools() throws Exception {
        String token = devMcpGrants.issue("0", -1L, -1L, -1L, AgentRole.DEVELOPER);
        Path dir = Files.createTempDirectory("ai-dev-real");
        Path config = dir.resolve("mcp.json");
        Files.writeString(config, "{\"mcpServers\":{\"erupt\":{\"type\":\"http\",\"url\":\"http://127.0.0.1:" + port
                + "/mcp\",\"headers\":{\"Authorization\":\"Bearer " + token + "\"}}}}");
        List<String> tools = new ArrayList<>(), results = new ArrayList<>();
        boolean[] success = new boolean[1];
        DevAgentProcess.Outcome outcome = devAgentProcess.run(-1L, claudeCodeRunner, new CodingAgentRunner.Spec(dir,
                "Call the erupt tool devRequirement, then call the erupt tool devReportProgress with percent 50 and phase "
                        + "\"verify\". Then reply with the single word done.", null, null, config, null, null), new CodingAgentRunner.Listener() {
            @Override
            public void session(String sessionId) {
            }

            @Override
            public void event(EventKind kind, String name, String content) {
                if (kind == EventKind.TOOL) tools.add(name);
                if (kind == EventKind.TOOL_RESULT) results.add(content);
            }

            @Override
            public void result(boolean ok, String text, Double costUsd, Long tokens) {
                success[0] = ok;
            }
        });
        devMcpGrants.revoke(token);
        assertEquals(0, outcome.exitCode(), outcome.stderrTail());
        assertTrue(success[0]);
        assertTrue(tools.contains("mcp__erupt__devReportProgress"), tools.toString());
        assertTrue(results.stream().anyMatch(it -> it.contains("Recorded 50%")), results.toString());
    }

}
