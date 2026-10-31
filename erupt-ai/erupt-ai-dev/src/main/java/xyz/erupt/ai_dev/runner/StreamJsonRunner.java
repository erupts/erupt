package xyz.erupt.ai_dev.runner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.ai_dev.constant.EventKind;

import java.util.List;

/**
 * The stream-json Claude Code made common, printed by Claude Code, Cursor CLI and Qwen Code: one JSON
 * message per line, {@code system} carrying the session id, {@code assistant} the text and tool calls,
 * {@code user} the tool results ({@code tool_call} events for Cursor), {@code result} the final answer.
 *
 * @author YuePeng
 * date 2026/10/23
 */
public abstract class StreamJsonRunner extends AbstractCliRunner {

    @Override
    public void onLine(String line, Listener listener) {
        if (StringUtils.isBlank(line) || !line.startsWith("{")) return;
        JsonObject msg = JsonParser.parseString(line).getAsJsonObject();
        switch (text(msg, "type")) {
            case "system" -> {
                if (msg.has("session_id")) listener.session(text(msg, "session_id"));
                if ("init".equals(text(msg, "subtype"))) listener.event(EventKind.SYSTEM, "init", text(msg, "model"));
            }
            case "assistant" -> {
                for (JsonElement item : content(msg)) {
                    JsonObject part = item.getAsJsonObject();
                    switch (text(part, "type")) {
                        case "text" -> listener.event(EventKind.TEXT, null, text(part, "text"));
                        case "tool_use" -> listener.event(EventKind.TOOL, text(part, "name"),
                                part.has("input") ? part.get("input").toString() : null);
                        default -> {
                        }
                    }
                }
            }
            case "user" -> {
                for (JsonElement item : content(msg)) {
                    JsonObject part = item.getAsJsonObject();
                    if ("tool_result".equals(text(part, "type"))) {
                        listener.event(EventKind.TOOL_RESULT, null, flatten(part.get("content")));
                    }
                }
            }
            // Cursor: one event when a tool starts and one when it completes, the tool keyed by name
            case "tool_call" -> {
                JsonObject call = object(msg, "tool_call");
                if (null == call || call.keySet().isEmpty()) return;
                String key = call.keySet().iterator().next();
                JsonObject body = object(call, key);
                String name = StringUtils.removeEnd(key, "ToolCall");
                if ("started".equals(text(msg, "subtype"))) listener.event(EventKind.TOOL, name, text(body, "args"));
                else if ("completed".equals(text(msg, "subtype"))) listener.event(EventKind.TOOL_RESULT, name, text(body, "result"));
            }
            case "result" -> {
                if (msg.has("session_id")) listener.session(text(msg, "session_id"));
                boolean success = "success".equals(text(msg, "subtype")) && !(msg.has("is_error") && msg.get("is_error").getAsBoolean());
                Double cost = msg.has("total_cost_usd") ? msg.get("total_cost_usd").getAsDouble() : null;
                Long tokens = null;
                if (msg.has("usage") && msg.get("usage").isJsonObject()) {
                    JsonObject usage = msg.getAsJsonObject("usage");
                    tokens = 0L;
                    for (String key : List.of("input_tokens", "output_tokens", "cache_creation_input_tokens", "cache_read_input_tokens")) {
                        if (usage.has(key) && usage.get(key).isJsonPrimitive()) tokens += usage.get(key).getAsLong();
                    }
                }
                listener.result(success, text(msg, "result"), cost, tokens);
            }
            default -> {
            }
        }
    }

    private static JsonArray content(JsonObject msg) {
        JsonElement message = msg.get("message");
        if (null == message || !message.isJsonObject()) return new JsonArray();
        JsonElement content = message.getAsJsonObject().get("content");
        return null != content && content.isJsonArray() ? content.getAsJsonArray() : new JsonArray();
    }

    // tool results come as a string or a list of text blocks
    private static String flatten(JsonElement content) {
        if (null == content || content.isJsonNull()) return null;
        if (content.isJsonPrimitive()) return content.getAsString();
        if (!content.isJsonArray()) return content.toString();
        StringBuilder sb = new StringBuilder();
        for (JsonElement item : content.getAsJsonArray()) {
            if (item.isJsonObject() && item.getAsJsonObject().has("text")) sb.append(item.getAsJsonObject().get("text").getAsString());
        }
        return sb.toString();
    }
}
