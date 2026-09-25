package xyz.erupt.decision;

import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.decision.answer.Answer;
import xyz.erupt.decision.constant.DecisionConst;
import xyz.erupt.decision.model.DecisionModel;
import xyz.erupt.decision.question.Question;
import xyz.erupt.decision.service.DecisionService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The way code asks for a decision.
 *
 * <pre>{@code
 * // questions are constants: declare once, reuse everywhere
 * static final Noul URGENT = Noul.of("Does this convey urgency?");
 * static final Choice<Dept> DEPT = Choice.of("Which team should handle this?", Dept.class);
 * static final Score MOOD = Score.of("How frustrated is the customer?", "Calm", "Frustrated", "Very angry");
 *
 * // one question, one line
 * if (Decisions.of(ticket).ask(URGENT).yes(0.9)) escalate(ticket);
 *
 * // several at once — the state is read once and every question answered against it
 * Decision decision = Decisions.of(ticket).ask(URGENT, DEPT, MOOD);
 * decision.get(DEPT).above(0.8).ifPresentOrElse(this::route, () -> queueForHuman(ticket));
 *
 * // or a decision declared in the admin, addressed by its code
 * Decision stored = Decisions.of(ticket).run("ticket_triage");
 * boolean refund = stored.noul("refund_requested").yes();
 * }</pre>
 *
 * @author YuePeng
 */
public final class Decisions {

    private final Object state;

    private DecisionModel config;

    private Decisions(Object state) {
        this.state = state;
    }

    /** The content to judge: a string, or any object worth handing over as structure */
    public static Decisions of(Object state) {
        return new Decisions(state);
    }

    /** Runs a stored decision straight away */
    public static Decision run(String code, Object state) {
        return Decisions.of(state).run(code);
    }

    /** Overrides the decision model that would otherwise be used */
    public Decisions using(DecisionModel config) {
        this.config = config;
        return this;
    }

    public Decisions using(String modelName) {
        return this.using(service().model(modelName));
    }

    /** One question, answered as the type that question returns */
    public <A extends Answer> A ask(Question<A> question) {
        return this.ask(new Question<?>[]{question}).get(question);
    }

    /** Several questions in one call, which is one charge and one round trip */
    public Decision ask(Question<?>... questions) {
        Map<String, Question<?>> asked = new LinkedHashMap<>();
        for (int i = 0; i < questions.length; i++) asked.put(DecisionConst.GENERATED_ID + i, questions[i]);
        DecisionService service = service();
        return service.evaluate(null == config ? service.defaultModel() : config, state, asked);
    }

    /** A decision declared in the admin, by its code */
    public Decision run(String code) {
        return service().run(code, state, config);
    }

    private static DecisionService service() {
        return EruptSpringUtil.getBean(DecisionService.class);
    }

}
