package xyz.erupt.decision.tool;

import com.google.gson.JsonElement;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.prompt.SystemPromptProvider;
import xyz.erupt.decision.Decision;
import xyz.erupt.decision.answer.Answer;
import xyz.erupt.decision.answer.ChoiceAnswer;
import xyz.erupt.decision.answer.NoulAnswer;
import xyz.erupt.decision.answer.ScoreAnswer;
import xyz.erupt.decision.constant.PrimitiveType;
import xyz.erupt.decision.model.DecisionDef;
import xyz.erupt.decision.model.DecisionQuestion;
import xyz.erupt.decision.service.DecisionService;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stored decisions as AI tools. An agent is a language model already, so an ad-hoc question buys
 * it nothing; a stored decision does — the rubric and the model were fixed by an administrator,
 * which makes the judgement the same one every caller gets.
 *
 * @author YuePeng
 */
@AiToolbox(platform = true)
@Component
public class DecisionTools implements SystemPromptProvider {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private DecisionService decisionService;

    @PostConstruct
    public void init() {
        SystemPromptProvider.registerProvider(this);
    }

    @Override
    public String getPrompt() {
        return """
                ## Decisions
                A stored decision is a judgement the administrators defined once: fixed questions, a
                fixed rubric and a dedicated model. When a task needs a classification, a score or a
                yes/no that `listDecisions` covers, call `runDecision` and act on its answer and
                confidence instead of judging it yourself.
                """;
    }

    @Tool("List the stored decisions: code, name, purpose and the questions each one answers")
    public String listDecisions() {
        return GsonFactory.getGson().toJson(eruptDao.lambdaQuery(DecisionDef.class)
                .eq(DecisionDef::getEnable, true).list().stream().map(Brief::of).toList());
    }

    @Tool("Run a stored decision against a state. Every question is answered with a confidence in 0..1: "
            + "a NOUL with yes/no and the probability of yes, a CHOICE with the winning option and the "
            + "probability of each option, a SCORE with a position across its ordered levels and the nearest level's label. "
            + "A low confidence means the answer should be escalated rather than acted on.")
    public String runDecision(@P("Decision code, from listDecisions") String code,
                              @P("The content to judge: plain text, or JSON to hand it over as structure") String state) {
        if (StringUtils.isBlank(state)) throw new EruptWebApiRuntimeException("A decision needs a state to judge");
        DecisionDef def = decisionService.def(code);
        return GsonFactory.getGson().toJson(Result.of(def, decisionService.run(code, DecisionQuestion.read(state))));
    }

    record Brief(String code, String name, String remark, List<Ask> questions) {

        static Brief of(DecisionDef def) {
            return new Brief(def.getCode(), def.getName(), def.getRemark(), def.getQuestions().stream().map(Ask::of).toList());
        }
    }

    // criteria is the rubric: the options of a choice, the levels of a score, what yes and no stand for
    record Ask(String code, PrimitiveType type, String instructions, JsonElement criteria) {

        static Ask of(DecisionQuestion question) {
            return new Ask(question.getCode(), question.getType(), question.getInstructions(),
                    StringUtils.isBlank(question.getCriteria()) ? null : DecisionQuestion.read(question.getCriteria()));
        }
    }

    record Result(String model, Map<String, Verdict> answers) {

        static Result of(DecisionDef def, Decision decision) {
            Map<String, String> asked = new LinkedHashMap<>();
            def.getQuestions().forEach(it -> asked.put(it.getCode(), it.getInstructions()));
            Map<String, Verdict> answers = new LinkedHashMap<>();
            decision.answers().forEach((code, answer) -> answers.put(code, Verdict.of(asked.get(code), answer)));
            return new Result(decision.model(), answers);
        }
    }

    /** One answer next to the question that produced it: a bare number decides nothing on its own */
    record Verdict(String question, PrimitiveType type, Object answer, String label, Double probability,
                   double confidence, Map<?, Double> probabilities) {

        static Verdict of(String question, Answer answer) {
            if (answer instanceof NoulAnswer noul) {
                return new Verdict(question, answer.type(), noul.yes(), null, noul.value(), answer.confidence(), null);
            }
            if (answer instanceof ChoiceAnswer<?> choice) {
                return new Verdict(question, answer.type(), choice.value(), null, null, answer.confidence(), choice.probabilities());
            }
            ScoreAnswer score = (ScoreAnswer) answer;
            return new Verdict(question, answer.type(), score.value(), score.label(), null, answer.confidence(), null);
        }
    }

}
