package xyz.erupt.decision.answer;

import xyz.erupt.decision.constant.PrimitiveType;

/**
 * The probability that a yes/no question is a yes, from 0 (no) to 1 (yes).
 *
 * @author YuePeng
 */
public record NoulAnswer(double value) implements Answer {

    @Override
    public PrimitiveType type() {
        return PrimitiveType.NOUL;
    }

    public boolean yes() {
        return this.yes(0.5);
    }

    public boolean yes(double minProbability) {
        return value >= minProbability;
    }

    /**
     * A noul carries no confidence of its own: the answer already is the probability, and how
     * far it sits from a 0.5 coin flip is the same measure the other primitives report.
     */
    @Override
    public double confidence() {
        return Math.abs(value - 0.5) * 2;
    }

}
