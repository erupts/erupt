package xyz.erupt.decision;

import xyz.erupt.decision.answer.Answer;
import xyz.erupt.decision.answer.ChoiceAnswer;
import xyz.erupt.decision.answer.NoulAnswer;
import xyz.erupt.decision.answer.ScoreAnswer;
import xyz.erupt.decision.answer.Usage;
import xyz.erupt.decision.question.Question;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The answers to one evaluation. Questions asked from java code are read back by the question
 * itself, which carries its answer type; questions from a stored definition are read by their
 * code, since only the definition knows them.
 *
 * @author YuePeng
 */
public final class Decision {

    // The model version that actually answered, which an alias hides
    private final String model;

    private final Usage usage;

    private final Map<String, Answer> answers;

    private final Map<Question<?>, String> ids;

    public Decision(String model, Usage usage, Map<String, Answer> answers, Map<Question<?>, String> ids) {
        this.model = model;
        this.usage = usage;
        this.answers = Collections.unmodifiableMap(answers);
        this.ids = new IdentityHashMap<>(ids);
    }

    public String model() {
        return model;
    }

    public Usage usage() {
        return usage;
    }

    public Set<String> ids() {
        return answers.keySet();
    }

    public Map<String, Answer> answers() {
        return answers;
    }

    /** The answer to a question asked from code, typed by the question that asked it */
    @SuppressWarnings("unchecked")
    public <A extends Answer> A get(Question<A> question) {
        String id = ids.get(question);
        if (null == id) throw new IllegalArgumentException("This question was not part of the evaluation");
        return (A) answers.get(id);
    }

    /** The answer to a question of a stored definition, by its code */
    public Answer get(String id) {
        Answer answer = answers.get(id);
        if (null == answer) throw new IllegalArgumentException("No answer named: " + id);
        return answer;
    }

    public NoulAnswer noul(String id) {
        return this.cast(id, NoulAnswer.class);
    }

    @SuppressWarnings("unchecked")
    public ChoiceAnswer<String> choice(String id) {
        return this.cast(id, ChoiceAnswer.class);
    }

    public ScoreAnswer score(String id) {
        return this.cast(id, ScoreAnswer.class);
    }

    private <A> A cast(String id, Class<A> type) {
        Answer answer = this.get(id);
        if (!type.isInstance(answer)) {
            throw new IllegalArgumentException("Question '" + id + "' answered a " + answer.type());
        }
        return type.cast(answer);
    }

}
