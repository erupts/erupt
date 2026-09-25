package xyz.erupt.decision.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.decision.Decision;
import xyz.erupt.decision.answer.Answer;
import xyz.erupt.decision.answer.ChoiceAnswer;
import xyz.erupt.decision.answer.NoulAnswer;
import xyz.erupt.decision.answer.ScoreAnswer;
import xyz.erupt.decision.model.DecisionDef;
import xyz.erupt.decision.model.DecisionQuestion;
import xyz.erupt.decision.model.DecisionTest;
import xyz.erupt.decision.service.DecisionService;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Runs a stored decision against a state typed into the dialog and writes the answers back into
 * the form. A bare number decides nothing on its own, so every answer is reported next to the
 * rubric that gave it meaning: what a yes stands for, what the winning option says, which level
 * a score landed on.
 *
 * @author YuePeng
 */
@Service
public class DecisionTestHandler implements OperationHandler<DecisionDef, DecisionTest>, EruptButtonHandler<DecisionTest> {

    private static final String YES = "true";

    private static final String NO = "false";

    @Resource
    private DecisionService decisionService;

    /** The row the button was clicked on names the decision under test */
    @Override
    public DecisionTest eruptFormValue(List<DecisionDef> data, DecisionTest form, String[] param) {
        form.setCode(data.get(0).getCode());
        return form;
    }

    /** There is nothing to submit: every run happens on the button, beside its answers */
    @Override
    public String exec(List<DecisionDef> data, DecisionTest form, String[] param) {
        return null;
    }

    @Override
    public String click(DecisionTest form, String[] params) {
        if (StringUtils.isBlank(form.getState())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("Test State")
                    + " " + I18nTranslate.$translate("erupt.notnull"));
        }
        DecisionDef def = decisionService.def(form.getCode());
        Decision decision = decisionService.run(form.getCode(), DecisionQuestion.read(form.getState()));
        // Read back by populateForm from this very instance, so the provider is asked once
        form.setAnswers(report(def, decision));
        return null;
    }

    @Override
    public Map<String, Object> populateForm(DecisionTest form, String[] params) {
        return null == form.getAnswers() ? Map.of()
                : Map.of(LambdaSee.field(DecisionTest::getAnswers), form.getAnswers());
    }

    private static String report(DecisionDef def, Decision decision) {
        Map<String, DecisionQuestion> asked = new LinkedHashMap<>();
        def.getQuestions().forEach(it -> asked.put(it.getCode(), it));
        StringBuilder report = new StringBuilder(decision.model())
                .append("   ").append(decision.usage().inputTokens()).append(" in / ")
                .append(decision.usage().outputTokens()).append(" out");
        decision.answers().forEach((code, answer) -> {
            DecisionQuestion question = asked.get(code);
            report.append("\n\n[").append(code).append("] ");
            if (null != question && StringUtils.isNotBlank(question.getInstructions())) {
                report.append(question.getInstructions());
            }
            report.append("\n").append(describe(question, answer));
        });
        return report.toString();
    }

    /** One answer, spelled out against the rubric of the question that produced it */
    private static String describe(DecisionQuestion question, Answer answer) {
        JsonElement criteria = null == question || StringUtils.isBlank(question.getCriteria()) ? null
                : DecisionQuestion.read(question.getCriteria());
        if (answer instanceof NoulAnswer noul) {
            // The probability is the answer; a confidence would only restate how far it sits from 0.5
            StringBuilder line = new StringBuilder(translate(noul.yes() ? "Yes" : "No"))
                    .append("   ").append(translate("Probability")).append(" ").append(number(noul.value()));
            String yes = text(criteria, YES);
            String no = text(criteria, NO);
            if (null != yes) line.append("\n").append(translate("Yes")).append(" = ").append(yes);
            if (null != no) line.append("\n").append(translate("No")).append(" = ").append(no);
            return line.toString();
        }
        if (answer instanceof ChoiceAnswer<?> choice) {
            String option = String.valueOf(choice.value());
            StringBuilder line = new StringBuilder(option).append(confidence(choice));
            String rubric = text(criteria, option);
            if (null != rubric) line.append("\n").append(option).append(" = ").append(rubric);
            if (choice.probabilities().size() > 1) {
                line.append("\n").append(translate("Distribution")).append(": ")
                        .append(distribution(choice.probabilities()));
            }
            return line.toString();
        }
        if (answer instanceof ScoreAnswer score) {
            JsonArray levels = null != criteria && criteria.isJsonArray() ? criteria.getAsJsonArray() : null;
            String label = null != score.label() ? score.label() : level(levels, score.level());
            StringBuilder line = new StringBuilder(number(score.value()));
            if (null != label) line.append(" ").append(label);
            if (null != levels) line.append(" (").append(score.level() + 1).append("/").append(levels.size()).append(")");
            line.append(confidence(score));
            if (null != levels) {
                line.append("\n").append(translate("Levels")).append(": ");
                for (int i = 0; i < levels.size(); i++) {
                    if (i > 0) line.append(" / ");
                    line.append(i + 1).append(" ").append(level(levels, i));
                }
            }
            return line.toString();
        }
        return "";
    }

    private static String confidence(Answer answer) {
        return "   " + translate("Confidence") + " " + number(answer.confidence());
    }

    private static String distribution(Map<?, Double> probabilities) {
        Comparator<Map.Entry<?, Double>> likeliestFirst = Comparator
                .<Map.Entry<?, Double>>comparingDouble(Map.Entry::getValue).reversed()
                .thenComparing(it -> String.valueOf(it.getKey()));
        return probabilities.entrySet().stream().map(it -> (Map.Entry<?, Double>) it).sorted(likeliestFirst)
                .map(it -> it.getKey() + " " + number(it.getValue()))
                .reduce((a, b) -> a + " / " + b).orElse("");
    }

    // A level describes itself with prose or with json, and is read back the way it was written
    private static String level(JsonArray levels, int index) {
        if (null == levels || index < 0 || index >= levels.size()) return null;
        JsonElement level = levels.get(index);
        return level.isJsonPrimitive() ? level.getAsString() : level.toString();
    }

    private static String text(JsonElement criteria, String key) {
        if (null == criteria || !criteria.isJsonObject()) return null;
        JsonObject json = criteria.getAsJsonObject();
        if (!json.has(key) || json.get(key).isJsonNull()) return null;
        JsonElement value = json.get(key);
        return value.isJsonPrimitive() ? value.getAsString() : value.toString();
    }

    private static String translate(String key) {
        return I18nTranslate.$translate(key);
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

}
