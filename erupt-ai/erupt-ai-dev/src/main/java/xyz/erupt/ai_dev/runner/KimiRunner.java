package xyz.erupt.ai_dev.runner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.constant.EventKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Kimi Code CLI: {@code kimi --print --output-format stream-json} (print mode approves on its own), the
 * prompt on stdin. Lines are chat messages: an assistant's text and tool calls, then the tool's answer.
 * The exit code and the last text decide the outcome. erupt's MCP server is passed with
 * {@code --mcp-config-file}.
 *
 * @author YuePeng
 * date 2026/10/23
 */
@Component
public class KimiRunner extends AbstractCliRunner {

    @Override
    public String code() {
        return "kimi";
    }

    @Override
    protected String defaultBinary() {
        return "kimi";
    }

    @Override
    protected String defaultLabel() {
        return "Kimi Code";
    }

    @Override
    public List<String> command(Spec spec) {
        List<String> command = new ArrayList<>(List.of(this.binary(), "--print", "--output-format", "stream-json",
                "-w", spec.workspace().toString()));
        command.addAll(this.args());
        if (StringUtils.isNotBlank(spec.model())) command.addAll(List.of("-m", CodingAgentRunner.safeArg(spec.model())));
        if (StringUtils.isNotBlank(spec.resumeSessionId())) command.addAll(List.of("-S", CodingAgentRunner.safeArg(spec.resumeSessionId())));
        if (null != spec.mcpConfig()) command.addAll(List.of("--mcp-config-file", spec.mcpConfig().toString()));
        return command;
    }

    @Override
    public void onLine(String line, Listener listener) {
        if (StringUtils.isBlank(line) || !line.startsWith("{")) return;
        JsonObject msg = JsonParser.parseString(line).getAsJsonObject();
        if (msg.has("session_id")) listener.session(text(msg, "session_id"));
        switch (text(msg, "role")) {
            case "assistant" -> {
                String content = content(msg.get("content"));
                if (StringUtils.isNotBlank(content)) listener.event(EventKind.TEXT, null, content);
                JsonElement calls = msg.get("tool_calls");
                if (null != calls && calls.isJsonArray()) {
                    for (JsonElement call : calls.getAsJsonArray()) {
                        JsonObject function = object(call.getAsJsonObject(), "function");
                        listener.event(EventKind.TOOL, text(function, "name"), text(function, "arguments"));
                    }
                }
            }
            case "tool" -> listener.event(EventKind.TOOL_RESULT, null, content(msg.get("content")));
            default -> {
            }
        }
    }

    // a string, or a list of typed parts
    private static String content(JsonElement content) {
        if (null == content || content.isJsonNull()) return null;
        if (content.isJsonPrimitive()) return content.getAsString();
        if (!content.isJsonArray()) return content.toString();
        StringBuilder sb = new StringBuilder();
        for (JsonElement item : (JsonArray) content) {
            if (item.isJsonObject() && item.getAsJsonObject().has("text")) sb.append(item.getAsJsonObject().get("text").getAsString());
        }
        return sb.toString();
    }

}
