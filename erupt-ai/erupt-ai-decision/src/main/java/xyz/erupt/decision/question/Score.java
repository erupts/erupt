package xyz.erupt.decision.question;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.decision.answer.ScoreAnswer;
import xyz.erupt.decision.constant.DecisionConst;
import xyz.erupt.decision.constant.PrimitiveType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rates the state against ordered levels. The answer is weighted across them, so it reports
 * "just past frustrated" rather than rounding the nuance away.
 *
 * @author YuePeng
 */
public final class Score implements Question<ScoreAnswer> {

    private final Object instructions;

    private final List<Object> levels;

    private Score(Object instructions, List<Object> levels) {
        if (levels.size() < DecisionConst.MIN_SCORE_LEVELS || levels.size() > DecisionConst.MAX_SCORE_LEVELS) {
            throw new IllegalArgumentException("A score question takes between " + DecisionConst.MIN_SCORE_LEVELS
                    + " and " + DecisionConst.MAX_SCORE_LEVELS + " levels, got " + levels.size());
        }
        this.instructions = instructions;
        this.levels = levels;
    }

    /** Levels in order, lowest first; each one describes what it takes to land there */
    public static Score of(Object instructions, Object... levels) {
        return new Score(instructions, List.of(levels));
    }

    @Override
    public PrimitiveType type() {
        return PrimitiveType.SCORE;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = Questions.base(this, instructions);
        JsonArray criteria = new JsonArray();
        levels.forEach(level -> criteria.add(Questions.json(level)));
        json.add("criteria", criteria);
        return json;
    }

    @Override
    public ScoreAnswer parse(JsonObject answer) {
        Map<Integer, String> legend = new LinkedHashMap<>();
        if (answer.has("legend")) {
            answer.getAsJsonObject("legend").entrySet()
                    .forEach(it -> legend.put(Integer.parseInt(it.getKey()), it.getValue().getAsString()));
        }
        Map<Integer, Double> probabilities = new LinkedHashMap<>();
        if (answer.has("probabilities")) {
            answer.getAsJsonObject("probabilities").entrySet()
                    .forEach(it -> probabilities.put(Integer.parseInt(it.getKey()), it.getValue().getAsDouble()));
        }
        return new ScoreAnswer(answer.get("score").getAsDouble(), Questions.confidence(answer), legend, probabilities);
    }

}
