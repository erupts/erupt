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
 * OpenCode: {@code opencode run --format json --standalone}, the prompt on stdin. Each line is a JSON
 * event carrying the {@code sessionID}; {@code text} parts are the answer, tool parts the calls, and
 * {@code error} a failure. There is no closing result event: the exit code and the last text decide.
 * erupt's MCP server reaches it through {@code OPENCODE_CONFIG_CONTENT}.
 *
 * @author YuePeng
 * date 2026/10/23
 */
@Component
public class OpenCodeRunner extends AbstractCliRunner {

    @Override
    public String code() {
        return "opencode";
    }

    @Override
    protected String defaultBinary() {
        return "opencode";
    }

    @Override
    protected String defaultLabel() {
        return "OpenCode";
    }

    // approve what is not explicitly denied: a headless run cannot be asked
    @Override
    protected List<String> defaultArgs() {
        return List.of("--auto");
    }

    // provider/model ids of every provider configured for OpenCode
    @Override
    public List<String> listModelsCommand() {
        return List.of(this.binary(), "models");
    }

    @Override
    public List<String> command(Spec spec) {
        // a private server per run, not the account's background service
        List<String> command = new ArrayList<>(List.of(this.binary(), "run", "--format", "json", "--standalone"));
        command.addAll(this.args());
        if (StringUtils.isNotBlank(spec.model())) command.addAll(List.of("-m", CodingAgentRunner.safeArg(spec.model())));
        if (StringUtils.isNotBlank(spec.resumeSessionId())) command.addAll(List.of("-s", CodingAgentRunner.safeArg(spec.resumeSessionId())));
        return command;
    }

    @Override
    public Map<String, String> environment(Spec spec) {
        if (null == spec.mcpUrl()) return Map.of();
        JsonObject headers = new JsonObject();
        headers.addProperty("Authorization", "Bearer " + spec.mcpToken());
        JsonObject server = new JsonObject();
        server.addProperty("type", "remote");
        server.addProperty("url", spec.mcpUrl());
        server.add("headers", headers);
        JsonObject mcp = new JsonObject();
        mcp.add(DevMcpGrants.SERVER, server);
        JsonObject config = new JsonObject();
        config.add("mcp", mcp);
        return Map.of("OPENCODE_CONFIG_CONTENT", config.toString());
    }

    @Override
    public void onLine(String line, Listener listener) {
        if (StringUtils.isBlank(line) || !line.startsWith("{")) return;
        JsonObject event = JsonParser.parseString(line).getAsJsonObject();
        if (event.has("sessionID")) listener.session(text(event, "sessionID"));
        JsonObject part = object(event, "part");
        String type = text(event, "type");
        if ("text".equals(type)) {
            listener.event(EventKind.TEXT, null, text(part, "text"));
        } else if ("error".equals(type)) {
            listener.event(EventKind.ERROR, null, text(object(event, "error"), "message"));
        } else if (type.startsWith("tool")) {
            JsonObject state = object(part, "state");
            listener.event(EventKind.TOOL, text(part, "tool"), text(state, "input"));
            if (null != state && state.has("output")) listener.event(EventKind.TOOL_RESULT, null, text(state, "output"));
        }
    }

}
