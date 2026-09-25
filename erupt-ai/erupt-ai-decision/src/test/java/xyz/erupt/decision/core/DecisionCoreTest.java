package xyz.erupt.decision.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import xyz.erupt.decision.Decision;
import xyz.erupt.decision.answer.ChoiceAnswer;
import xyz.erupt.decision.answer.ScoreAnswer;
import xyz.erupt.decision.model.DecisionModel;
import xyz.erupt.decision.question.Choice;
import xyz.erupt.decision.question.Noul;
import xyz.erupt.decision.question.Question;
import xyz.erupt.decision.question.Score;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives a full evaluation against a canned provider body — the answer shapes are the ones
 * published in the System One API reference. No request leaves the process.
 *
 * @author YuePeng
 */
public class DecisionCoreTest {

    enum Dept {
        BILLING, TECHNICAL, SALES
    }

    private static final String RESPONSE = """
            {
              "model": "jev-1.13.0",
              "answers": {
                "is_urgent": { "type": "noul", "noul": 0.95 },
                "department": {
                  "type": "choice",
                  "choice": "BILLING",
                  "probabilities": { "BILLING": 0.88, "TECHNICAL": 0.12, "SALES": 0.0 },
                  "confidence": 0.81
                },
                "frustration": {
                  "type": "score",
                  "score": 1.05,
                  "legend": { "0": "Calm", "1": "Frustrated", "2": "Very angry" },
                  "probabilities": { "0": 0.0, "1": 0.95, "2": 0.05 },
                  "confidence": 0.92
                }
              },
              "usage": { "input_tokens": 318, "output_tokens": 34 }
            }
            """;

    /** Captures what would have been posted, and answers with the canned body */
    static class FakeCore extends DecisionCore {

        JsonObject request;

        @Override
        public String code() {
            return "Fake";
        }

        @Override
        public String api() {
            return "https://fake";
        }

        @Override
        public String model() {
            return "fake-latest";
        }

        @Override
        protected JsonObject call(DecisionModel config, JsonObject request) {
            this.request = request;
            return JsonParser.parseString(RESPONSE).getAsJsonObject();
        }
    }

    private static DecisionModel config() {
        DecisionModel config = new DecisionModel();
        config.setName("fake");
        config.setProvider("Fake");
        config.setModel("jev-latest");
        return config;
    }

    private static Map<String, Question<?>> questions(Question<?> urgent, Question<?> dept, Question<?> mood) {
        Map<String, Question<?>> questions = new LinkedHashMap<>();
        questions.put("is_urgent", urgent);
        questions.put("department", dept);
        questions.put("frustration", mood);
        return questions;
    }

    @Test
    public void everyQuestionIsAnsweredThroughTheQuestionThatAskedIt() {
        Noul urgent = Noul.of("Does this convey urgency?");
        Choice<Dept> dept = Choice.of("Which team should handle this?", Dept.class);
        Score mood = Score.of("How frustrated is the customer?", "Calm", "Frustrated", "Very angry");
        FakeCore core = new FakeCore();

        Decision decision = core.evaluate(config(), "Help! My payouts have been failing for 3 days.",
                questions(urgent, dept, mood));

        assertEquals("jev-1.13.0", decision.model());
        assertEquals(318, decision.usage().inputTokens());
        assertEquals(0.95, decision.get(urgent).value());
        assertTrue(decision.get(urgent).yes(0.9));
        assertEquals(Dept.BILLING, decision.get(dept).value());
        assertEquals(0.12, decision.get(dept).probability(Dept.TECHNICAL));
        assertEquals(1, decision.get(mood).level());
        assertEquals("Frustrated", decision.get(mood).label());
    }

    @Test
    public void theModelAndTheStateRideAlongWithTheQuestions() {
        FakeCore core = new FakeCore();
        core.evaluate(config(), Map.of("ticket", "payouts failing"),
                questions(Noul.of("urgent?"), Choice.of("team?", Dept.class),
                        Score.of("mood?", "Calm", "Frustrated", "Very angry")));

        assertEquals("jev-latest", core.request.get("model").getAsString());
        assertEquals("payouts failing", core.request.getAsJsonObject("state").get("ticket").getAsString());
        assertEquals(3, core.request.getAsJsonObject("questions").size());
    }

    @Test
    public void confidenceIsWhatDecidesWhetherToActOnAnAnswer() {
        FakeCore core = new FakeCore();
        Choice<Dept> dept = Choice.of("Which team?", Dept.class);
        Score mood = Score.of("How frustrated?", "Calm", "Frustrated", "Very angry");
        Decision decision = core.evaluate(config(), "…", questions(Noul.of("urgent?"), dept, mood));

        ChoiceAnswer<Dept> answer = decision.get(dept);
        assertTrue(answer.above(0.8).isPresent());
        assertTrue(answer.above(0.9).isEmpty());

        ScoreAnswer score = decision.get(mood);
        assertTrue(score.confident(0.9));
        assertFalse(score.confident(0.95));
    }

    @Test
    public void aStoredDecisionIsReadBackByItsQuestionCode() {
        FakeCore core = new FakeCore();
        Decision decision = core.evaluate(config(), "…",
                questions(Noul.of("urgent?"), Choice.of("team?", "BILLING", "TECHNICAL", "SALES"),
                        Score.of("mood?", "Calm", "Frustrated", "Very angry")));

        assertTrue(decision.noul("is_urgent").yes());
        assertEquals("BILLING", decision.choice("department").value());
        assertEquals(1.05, decision.score("frustration").value());
    }

}
