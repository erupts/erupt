package xyz.erupt.decision.answer;

import xyz.erupt.decision.constant.PrimitiveType;

import java.util.Map;
import java.util.Optional;

/**
 * The winning option plus the probability of every option that was offered.
 *
 * @param <T> the option type — the enum constant when the question was asked with an enum,
 *            the option key otherwise
 * @author YuePeng
 */
public record ChoiceAnswer<T>(T value, double confidence, Map<T, Double> probabilities) implements Answer {

    @Override
    public PrimitiveType type() {
        return PrimitiveType.CHOICE;
    }

    public double probability(T option) {
        return probabilities.getOrDefault(option, 0d);
    }

    /** The winning option, but only when the model was sure enough; empty means escalate */
    public Optional<T> above(double minConfidence) {
        return this.confident(minConfidence) ? Optional.of(value) : Optional.empty();
    }

}
