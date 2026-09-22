package xyz.erupt.decision.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.decision.answer.Answer;
import xyz.erupt.decision.constant.PrimitiveType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One typed question put to a System One model. A question is an immutable value: declare it
 * once as a constant and reuse it, the model holds no state between calls.
 *
 * @param <A> the answer this question comes back as
 * @author YuePeng
 */
public interface Question<A extends Answer> {

    PrimitiveType type();

    /** The question in wire form */
    JsonElement toJson();

    /** Reads the answer the model returned for this question */
    A parse(JsonObject answer);

    /**
     * Builds a question from a stored definition, where the type is known only at runtime and
     * the rubric arrives as raw json.
     */
    static Question<?> of(PrimitiveType type, Object instructions, JsonElement criteria) {
        switch (type) {
            case NOUL -> {
                Noul noul = Noul.of(instructions);
                return null == criteria || !criteria.isJsonObject() ? noul
                        : noul.criteria(criteria.getAsJsonObject().get("true"), criteria.getAsJsonObject().get("false"));
            }
            case CHOICE -> {
                if (null == criteria || !criteria.isJsonObject()) {
                    throw new IllegalArgumentException("A choice question needs a criteria object of option to description");
                }
                Map<String, Object> options = new LinkedHashMap<>();
                criteria.getAsJsonObject().entrySet().forEach(it -> options.put(it.getKey(), it.getValue()));
                return Choice.of(instructions, options);
            }
            case SCORE -> {
                if (null == criteria || !criteria.isJsonArray()) {
                    throw new IllegalArgumentException("A score question needs a criteria array of ordered levels");
                }
                JsonArray levels = criteria.getAsJsonArray();
                Object[] values = new Object[levels.size()];
                for (int i = 0; i < levels.size(); i++) values[i] = levels.get(i);
                return Score.of(instructions, values);
            }
            default -> throw new IllegalArgumentException("Unsupported question type: " + type);
        }
    }

}
