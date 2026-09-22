package xyz.erupt.decision.question;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Wire helpers shared by the three question types.
 *
 * @author YuePeng
 */
final class Questions {

    // Questions are built as static constants long before spring is up, so they carry their own
    private static final Gson GSON = new Gson();

    private Questions() {
    }

    /** Instructions and rubrics accept a string, an object or an array, all the way down */
    static JsonElement json(Object value) {
        return null == value ? JsonNull.INSTANCE : GSON.toJsonTree(value);
    }

    static JsonObject base(Question<?> question, Object instructions) {
        JsonObject json = new JsonObject();
        json.addProperty("type", question.type().wire());
        json.add("instructions", json(instructions));
        return json;
    }

    static double confidence(JsonObject answer) {
        return answer.has("confidence") ? answer.get("confidence").getAsDouble() : 0d;
    }

}
