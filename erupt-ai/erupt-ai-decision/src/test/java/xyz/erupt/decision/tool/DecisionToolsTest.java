package xyz.erupt.decision.tool;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.decision.Decision;
import xyz.erupt.decision.answer.Answer;
import xyz.erupt.decision.answer.ChoiceAnswer;
import xyz.erupt.decision.answer.NoulAnswer;
import xyz.erupt.decision.answer.ScoreAnswer;
import xyz.erupt.decision.answer.Usage;
import xyz.erupt.decision.constant.PrimitiveType;
import xyz.erupt.decision.model.DecisionDef;
import xyz.erupt.decision.model.DecisionQuestion;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What an agent reads back from the decision tools: the json is the contract, so it is asserted
 * as json. No provider is asked — the answers are built by hand.
 *
 * @author YuePeng
 */
class DecisionToolsTest {

    private static final String URGENT = "urgent";

    private static final String DEPT = "dept";

    private static final String MOOD = "mood";

    private static final String BILLING = "BILLING";

    @Test
    void everyAnswerIsReportedNextToTheQuestionThatAskedIt() {
        Map<String, Answer> answers = new LinkedHashMap<>();
        answers.put(URGENT, new NoulAnswer(0.95));
        answers.put(DEPT, new ChoiceAnswer<>(BILLING, 0.81, Map.of(BILLING, 0.88, "SALES", 0.12)));
        answers.put(MOOD, new ScoreAnswer(1.05, 0.92, Map.of(0, "Calm", 1, "Frustrated", 2, "Very angry"), Map.of()));
        Decision decision = new Decision("jev-1.13.0", new Usage(318, 34), answers, Map.of());

        JsonObject result = GsonFactory.getGson().toJsonTree(DecisionTools.Result.of(def(), decision)).getAsJsonObject();
        assertEquals("jev-1.13.0", result.get("model").getAsString());
        JsonObject verdicts = result.getAsJsonObject("answers");

        JsonObject urgent = verdicts.getAsJsonObject(URGENT);
        assertEquals("Is it urgent?", urgent.get("question").getAsString());
        assertTrue(urgent.get("answer").getAsBoolean());
        assertEquals(0.95, urgent.get("probability").getAsDouble(), 1e-9);
        assertEquals(0.9, urgent.get("confidence").getAsDouble(), 1e-9);
        assertTrue(urgent.get("probabilities").isJsonNull(), "a yes/no has no distribution to report");

        JsonObject dept = verdicts.getAsJsonObject(DEPT);
        assertEquals(BILLING, dept.get("answer").getAsString());
        assertEquals(0.88, dept.getAsJsonObject("probabilities").get(BILLING).getAsDouble(), 1e-9);
        assertTrue(dept.get("probability").isJsonNull());

        JsonObject mood = verdicts.getAsJsonObject(MOOD);
        assertEquals(1.05, mood.get("answer").getAsDouble(), 1e-9);
        assertEquals("Frustrated", mood.get("label").getAsString());
        assertEquals(PrimitiveType.SCORE.name(), mood.get("type").getAsString());
    }

    @Test
    void aListedDecisionCarriesItsRubricAsStructure() {
        JsonObject brief = GsonFactory.getGson().toJsonTree(DecisionTools.Brief.of(def())).getAsJsonObject();
        assertEquals("triage", brief.get("code").getAsString());
        assertEquals(3, brief.getAsJsonArray("questions").size());
        JsonObject dept = brief.getAsJsonArray("questions").get(1).getAsJsonObject();
        assertEquals(DEPT, dept.get("code").getAsString());
        assertEquals("Anything about money", dept.getAsJsonObject("criteria").get(BILLING).getAsString());
        assertTrue(brief.getAsJsonArray("questions").get(0).getAsJsonObject().get("criteria").isJsonNull(), "no rubric to show");
    }

    private static DecisionDef def() {
        DecisionDef def = new DecisionDef();
        def.setCode("triage");
        def.setName("Ticket triage");
        def.getQuestions().add(question(URGENT, PrimitiveType.NOUL, "Is it urgent?", null));
        def.getQuestions().add(question(DEPT, PrimitiveType.CHOICE, "Which team?", "{\"" + BILLING + "\":\"Anything about money\"}"));
        def.getQuestions().add(question(MOOD, PrimitiveType.SCORE, "How frustrated?", "[\"Calm\",\"Frustrated\",\"Very angry\"]"));
        return def;
    }

    private static DecisionQuestion question(String code, PrimitiveType type, String instructions, String criteria) {
        DecisionQuestion question = new DecisionQuestion();
        question.setCode(code);
        question.setType(type);
        question.setInstructions(instructions);
        question.setCriteria(criteria);
        return question;
    }

}
