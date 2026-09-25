package xyz.erupt.decision.question;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import xyz.erupt.decision.answer.ChoiceAnswer;
import xyz.erupt.decision.constant.DecisionConst;
import xyz.erupt.decision.constant.PrimitiveType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Picks one option from a closed set. Asked with an enum it answers with that enum, so a
 * decision never reaches the rest of the program as a bare string.
 *
 * @param <T> the option type handed back in the answer
 * @author YuePeng
 */
public final class Choice<T> implements Question<ChoiceAnswer<T>> {

    private final Object instructions;

    // Option key to its rubric; a null rubric means the key speaks for itself
    private final Map<String, Object> criteria;

    private final Function<String, T> decode;

    private Choice(Object instructions, Map<String, Object> criteria, Function<String, T> decode) {
        if (criteria.isEmpty()) {
            throw new IllegalArgumentException("A choice question needs at least one option");
        }
        if (criteria.size() > DecisionConst.MAX_CHOICE_OPTIONS) {
            throw new IllegalArgumentException("A choice question accepts at most "
                    + DecisionConst.MAX_CHOICE_OPTIONS + " options, got " + criteria.size());
        }
        this.instructions = instructions;
        this.criteria = criteria;
        this.decode = decode;
    }

    /**
     * Options taken from an enum, answered as that enum. Constants implementing
     * {@link Criteria} describe themselves to the model; the rest go by name alone.
     */
    public static <E extends Enum<E>> Choice<E> of(Object instructions, Class<E> options) {
        Map<String, Object> criteria = new LinkedHashMap<>();
        for (E option : options.getEnumConstants()) {
            criteria.put(option.name(), option instanceof Criteria it ? it.criteria() : null);
        }
        return new Choice<>(instructions, criteria, name -> Enum.valueOf(options, name));
    }

    /** Options named inline, each with the rubric that tells the model when it applies */
    public static Choice<String> of(Object instructions, Map<String, ?> criteria) {
        return new Choice<>(instructions, new LinkedHashMap<>(criteria), Function.identity());
    }

    /** Options that need no rubric beyond their own name */
    public static Choice<String> of(Object instructions, String... options) {
        Map<String, Object> criteria = new LinkedHashMap<>();
        for (String option : options) criteria.put(option, null);
        return new Choice<>(instructions, criteria, Function.identity());
    }

    @Override
    public PrimitiveType type() {
        return PrimitiveType.CHOICE;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = Questions.base(this, instructions);
        JsonObject options = new JsonObject();
        // An option with no rubric still has to reach the model, so null is written out
        criteria.forEach((option, rubric) -> options.add(option, null == rubric ? JsonNull.INSTANCE : Questions.json(rubric)));
        json.add("criteria", options);
        return json;
    }

    @Override
    public ChoiceAnswer<T> parse(JsonObject answer) {
        Map<T, Double> probabilities = new LinkedHashMap<>();
        if (answer.has("probabilities")) {
            answer.getAsJsonObject("probabilities").entrySet()
                    .forEach(it -> probabilities.put(decode.apply(it.getKey()), it.getValue().getAsDouble()));
        }
        return new ChoiceAnswer<>(decode.apply(answer.get("choice").getAsString()),
                Questions.confidence(answer), probabilities);
    }

}
