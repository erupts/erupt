package xyz.erupt.decision.constant;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The three question types a System One model answers. The wire name is the lowercase
 * constant, so the enum is the only place that knows the protocol spelling.
 *
 * @author YuePeng
 */
public enum PrimitiveType {

    // Is this true? Answered with the probability of yes
    NOUL,

    // Which of these options? Answered with the winning option and the full distribution
    CHOICE,

    // Which level? Answered with a probability-weighted position across ordered levels
    SCORE,
    ;

    public String wire() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static PrimitiveType of(String wire) {
        return valueOf(wire.toUpperCase(Locale.ROOT));
    }

    public static class H implements ChoiceFetchHandler<Void> {

        @Override
        public List<VLModel> fetch(String[] params) {
            return Arrays.stream(PrimitiveType.values()).map(it -> new VLModel(it.name(), it.name())).toList();
        }
    }

}
