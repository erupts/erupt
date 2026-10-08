package xyz.erupt.ai_tune.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import xyz.erupt.ai_tune.constants.DatasetFormat;

import java.util.Set;

/**
 * Checks one JSONL line against the dataset format the way a provider would before it
 * accepts the file, so problems surface in the console instead of as a failed job an hour
 * later. Also produces the turn count and a token estimate used for the dataset totals.
 *
 * @author YuePeng
 * date 2026/10/8
 */
public final class SampleValidator {

    private SampleValidator() {
    }

    private static final Set<String> ROLES = Set.of("system", "developer", "user", "assistant", "tool");

    @Getter
    public static final class Result {

        private boolean valid = true;

        private String error;

        private int turns;

        private int tokens;

        private Result fail(String error) {
            this.valid = false;
            this.error = error;
            return this;
        }

    }

    public static Result validate(String format, String content) {
        Result result = new Result();
        if (null == content || content.isBlank()) return result.fail("Empty line");
        JsonElement root;
        try {
            root = JsonParser.parseString(content);
        } catch (JsonSyntaxException e) {
            return result.fail("Invalid JSON: " + firstLine(e.getMessage()));
        }
        if (!root.isJsonObject()) return result.fail("Each line must be a JSON object");
        JsonObject obj = root.getAsJsonObject();
        if (DatasetFormat.PREFERENCE.equals(format)) {
            return validatePreference(obj, result);
        }
        return validateChat(obj, result);
    }

    private static Result validateChat(JsonObject obj, Result result) {
        if (!obj.has("messages") || !obj.get("messages").isJsonArray()) return result.fail("Missing \"messages\" array");
        JsonArray messages = obj.getAsJsonArray("messages");
        if (messages.size() < 2) return result.fail("At least two messages are required");
        boolean hasAssistant = false, hasUser = false;
        for (int i = 0; i < messages.size(); i++) {
            String error = checkMessage(messages.get(i), i);
            if (null != error) return result.fail(error);
            String role = messages.get(i).getAsJsonObject().get("role").getAsString();
            hasAssistant |= "assistant".equals(role);
            hasUser |= "user".equals(role);
            result.tokens += estimateTokens(messages.get(i).getAsJsonObject());
        }
        if (!hasUser) return result.fail("No user message");
        if (!hasAssistant) return result.fail("No assistant message to learn from");
        String last = messages.get(messages.size() - 1).getAsJsonObject().get("role").getAsString();
        if (!"assistant".equals(last)) return result.fail("The last message must come from the assistant");
        result.turns = messages.size();
        if (obj.has("tools") && obj.get("tools").isJsonArray()) {
            result.tokens += estimateTokens(obj.get("tools").toString());
        }
        return result;
    }

    private static Result validatePreference(JsonObject obj, Result result) {
        JsonObject input = obj.has("input") && obj.get("input").isJsonObject() ? obj.getAsJsonObject("input") : null;
        if (null == input || !input.has("messages") || !input.get("messages").isJsonArray()) {
            return result.fail("Missing \"input.messages\" array");
        }
        JsonArray messages = input.getAsJsonArray("messages");
        if (messages.isEmpty()) return result.fail("\"input.messages\" is empty");
        for (int i = 0; i < messages.size(); i++) {
            String error = checkMessage(messages.get(i), i);
            if (null != error) return result.fail(error);
            result.tokens += estimateTokens(messages.get(i).getAsJsonObject());
        }
        for (String key : new String[]{"preferred_output", "non_preferred_output"}) {
            if (!obj.has(key) || !obj.get(key).isJsonArray() || obj.getAsJsonArray(key).isEmpty()) {
                return result.fail("Missing \"" + key + "\" array");
            }
            for (JsonElement el : obj.getAsJsonArray(key)) {
                String error = checkMessage(el, -1);
                if (null != error) return result.fail(key + ": " + error);
                if (!"assistant".equals(el.getAsJsonObject().get("role").getAsString())) {
                    return result.fail("\"" + key + "\" must hold assistant messages");
                }
                result.tokens += estimateTokens(el.getAsJsonObject());
            }
        }
        result.turns = messages.size() + 1;
        return result;
    }

    private static String checkMessage(JsonElement el, int index) {
        String at = index < 0 ? "Message" : "Message #" + (index + 1);
        if (!el.isJsonObject()) return at + " is not an object";
        JsonObject msg = el.getAsJsonObject();
        if (!msg.has("role") || !msg.get("role").isJsonPrimitive()) return at + " has no role";
        String role = msg.get("role").getAsString();
        if (!ROLES.contains(role)) return at + " has unknown role \"" + role + "\"";
        boolean hasContent = msg.has("content") && !msg.get("content").isJsonNull();
        boolean hasToolCalls = msg.has("tool_calls") && msg.get("tool_calls").isJsonArray();
        if (!hasContent && !("assistant".equals(role) && hasToolCalls)) return at + " has no content";
        if (hasContent && !msg.get("content").isJsonPrimitive() && !msg.get("content").isJsonArray()) {
            return at + " content must be a string or a content-part array";
        }
        return null;
    }

    private static int estimateTokens(JsonObject message) {
        int tokens = 4; // per-message framing
        if (message.has("content") && !message.get("content").isJsonNull()) {
            JsonElement content = message.get("content");
            tokens += estimateTokens(content.isJsonPrimitive() ? content.getAsString() : content.toString());
        }
        if (message.has("tool_calls")) tokens += estimateTokens(message.get("tool_calls").toString());
        return tokens;
    }

    /**
     * Rough tokeniser-free estimate: a CJK character is about one token, everything else
     * about four characters per token. Good enough for sizing and cost hints.
     */
    public static int estimateTokens(String text) {
        if (null == text || text.isEmpty()) return 0;
        int cjk = 0, other = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            if (isCjk(cp)) cjk++;
            else if (!Character.isWhitespace(cp)) other++;
        }
        return cjk + (int) Math.ceil(other / 4.0);
    }

    private static boolean isCjk(int cp) {
        Character.UnicodeScript script = Character.UnicodeScript.of(cp);
        return script == Character.UnicodeScript.HAN || script == Character.UnicodeScript.HIRAGANA
                || script == Character.UnicodeScript.KATAKANA || script == Character.UnicodeScript.HANGUL;
    }

    private static String firstLine(String message) {
        if (null == message) return "";
        int nl = message.indexOf('\n');
        return nl < 0 ? message : message.substring(0, nl);
    }

}
