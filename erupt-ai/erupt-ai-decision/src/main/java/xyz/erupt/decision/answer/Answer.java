package xyz.erupt.decision.answer;

import xyz.erupt.decision.constant.PrimitiveType;

/**
 * One typed answer. Every answer carries a confidence in 0..1 derived from its probability
 * distribution: the answer says <i>what</i>, the confidence says <i>whether to act on it</i>.
 *
 * @author YuePeng
 */
public sealed interface Answer permits NoulAnswer, ChoiceAnswer, ScoreAnswer {

    PrimitiveType type();

    double confidence();

    default boolean confident(double minConfidence) {
        return this.confidence() >= minConfidence;
    }

}
