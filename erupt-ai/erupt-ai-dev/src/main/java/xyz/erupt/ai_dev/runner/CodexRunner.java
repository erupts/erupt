package xyz.erupt.ai_dev.runner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.constant.EventKind;
import xyz.erupt.ai_dev.mcp.DevMcpGrants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Codex CLI: {@code codex exec --json}, the prompt on stdin ({@code -}). Each line is a JSON event:
 * {@code thread.started} carries the session, {@code item.completed} the messages, commands and tool calls,
 * {@code turn.completed} the usage, {@code turn.failed} / {@code error} a failure. erupt's MCP server is
 * added with {@code -c} overrides; its token is read from the environment, never from an argument.
 *
 * @author YuePeng
 * date 2026/10/23
 */
@Component
public class CodexRunner extends AbstractCliRunner {

    private static final String TOKEN_ENV = "ERUPT_MCP_TOKEN";

    @Override
    public String code() {
        return "codex";
    }

    @Override
    protected String defaultBinary() {
        return "codex";
    }

    @Override
    protected String defaultLabel() {
        return "Codex";
    }

    // edits inside the workspace, no approval prompts
    @Override
    protected List<String> defaultArgs() {
        return List.of("--full-auto");
    }

    @Override
    public List<String> command(Spec spec) {
        // the workspace holds one worktree per repository and is no repository itself
        List<String> command = new ArrayList<>(List.of(this.binary(), "exec", "--json", "--skip-git-repo-check"));
        command.addAll(this.args());
        if (StringUtils.isNotBlank(spec.model())) command.addAll(List.of("-m", CodingAgentRunner.safeArg(spec.model())));
        if (null != spec.mcpUrl()) {
            String server = "mcp_servers." + DevMcpGrants.SERVER;
            command.addAll(List.of("-c", server + ".url=\"" + spec.mcpUrl().replace("\"", "") + "\"",
                    "-c", server + ".bearer_token_env_var=\"" + TOKEN_ENV + "\""));
        }
        if (StringUtils.isNotBlank(spec.resumeSessionId())) command.addAll(List.of("resume", CodingAgentRunner.safeArg(spec.resumeSessionId())));
        command.add("-");
        return command;
    }

    @Override
    public Map<String, String> environment(Spec spec) {
        return null == spec.mcpToken() ? Map.of() : Map.of(TOKEN_ENV, spec.mcpToken());
    }

    @Override
    public void onLine(String line, Listener listener) {
        if (StringUtils.isBlank(line) || !line.startsWith("{")) return;
        JsonObject event = JsonParser.parseString(line).getAsJsonObject();
        switch (text(event, "type")) {
            case "thread.started" -> listener.session(text(event, "thread_id"));
            case "item.completed" -> this.item(object(event, "item"), listener);
            case "turn.completed" -> listener.result(true, null, null,
                    number(object(event, "usage"), "input_tokens", "cached_input_tokens", "output_tokens"));
            case "turn.failed" -> listener.result(false, text(object(event, "error"), "message"), null, null);
            case "error" -> listener.event(EventKind.ERROR, null, text(event, "message"));
            default -> {
            }
        }
    }

    private void item(JsonObject item, Listener listener) {
        if (null == item) return;
        switch (text(item, "type")) {
            case "agent_message" -> listener.event(EventKind.TEXT, null, text(item, "text"));
            case "command_execution" -> {
                listener.event(EventKind.TOOL, "shell", text(item, "command"));
                listener.event(EventKind.TOOL_RESULT, null, text(item, "aggregated_output"));
            }
            case "file_change" -> listener.event(EventKind.TOOL, "edit", text(item, "changes"));
            case "mcp_tool_call" -> listener.event(EventKind.TOOL, text(item, "server") + "." + text(item, "tool"), text(item, "arguments"));
            case "error" -> listener.event(EventKind.ERROR, null, text(item, "message"));
            default -> {
            }
        }
    }

}
