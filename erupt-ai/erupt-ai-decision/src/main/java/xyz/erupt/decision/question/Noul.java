package xyz.erupt.decision.question;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.decision.answer.NoulAnswer;
import xyz.erupt.decision.constant.PrimitiveType;

/**
 * A yes/no question. The answer is the probability of yes, never a hard boolean — where the
 * cut-off sits is the caller's decision, not the model's.
 *
 * @author YuePeng
 */
public final class Noul implements Question<NoulAnswer> {

    private final Object instructions;

    private Object yes;

    private Object no;

    private Noul(Object instructions) {
        this.instructions = instructions;
    }

    public static Noul of(Object instructions) {
        return new Noul(instructions);
    }

    /** Optional: spell out what a yes and a no mean, when the question alone leaves room */
    public Noul criteria(Object yes, Object no) {
        this.yes = yes;
        this.no = no;
        return this;
    }

    @Override
    public PrimitiveType type() {
        return PrimitiveType.NOUL;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = Questions.base(this, instructions);
        if (null != yes || null != no) {
            JsonObject criteria = new JsonObject();
            if (null != yes) criteria.add("true", Questions.json(yes));
            if (null != no) criteria.add("false", Questions.json(no));
            json.add("criteria", criteria);
        }
        return json;
    }

    @Override
    public NoulAnswer parse(JsonObject answer) {
        return new NoulAnswer(answer.get("noul").getAsDouble());
    }

}
