package xyz.erupt.decision.answer;

import xyz.erupt.decision.constant.PrimitiveType;

import java.util.Map;
import java.util.Optional;

/**
 * A probability-weighted position across the ordered levels the question defined, so it can
 * land between two of them — 1.05 on a calm/frustrated/angry scale is "frustrated, a shade angry".
 *
 * @author YuePeng
 */
public record ScoreAnswer(double value, double confidence, Map<Integer, String> legend,
                          Map<Integer, Double> probabilities) implements Answer {

    @Override
    public PrimitiveType type() {
        return PrimitiveType.SCORE;
    }

    /** The nearest whole level, for code that wants to switch rather than threshold */
    public int level() {
        return (int) Math.round(value);
    }

    public String label() {
        return legend.get(this.level());
    }

    public double probability(int level) {
        return probabilities.getOrDefault(level, 0d);
    }

    public Optional<Double> above(double minConfidence) {
        return this.confident(minConfidence) ? Optional.of(value) : Optional.empty();
    }

}
