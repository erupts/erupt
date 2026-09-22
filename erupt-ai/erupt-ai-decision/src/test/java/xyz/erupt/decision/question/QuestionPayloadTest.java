package xyz.erupt.decision.question;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The wire shape is the contract with the provider, so each primitive is pinned to the
 * documented payload — including the null rubric a bare option must still be sent with.
 *
 * @author YuePeng
 */
public class QuestionPayloadTest {

    enum Dept implements Criteria {

        BILLING("Payments, invoicing, refunds"),
        TECHNICAL("Bugs, outages, integrations"),
        ;

        private final String criteria;

        Dept(String criteria) {
            this.criteria = criteria;
        }

        @Override
        public String criteria() {
            return this.criteria;
        }
    }

    enum Plan {
        FREE, PRO
    }

    @Test
    public void noulCarriesNoCriteriaUntilItIsGiven() {
        JsonObject json = Noul.of("Does this convey urgency?").toJson().getAsJsonObject();
        assertEquals("noul", json.get("type").getAsString());
        assertEquals("Does this convey urgency?", json.get("instructions").getAsString());
        assertTrue(json.get("criteria") == null);

        JsonObject described = Noul.of("Does this convey urgency?")
                .criteria("Explicitly time-sensitive", "No urgency expressed").toJson().getAsJsonObject();
        assertEquals("Explicitly time-sensitive", described.getAsJsonObject("criteria").get("true").getAsString());
        assertEquals("No urgency expressed", described.getAsJsonObject("criteria").get("false").getAsString());
    }

    @Test
    public void choiceDescribesItselfFromTheEnum() {
        JsonObject json = Choice.of("Which team should handle this?", Dept.class).toJson().getAsJsonObject();
        assertEquals("choice", json.get("type").getAsString());
        JsonObject criteria = json.getAsJsonObject("criteria");
        assertEquals("Payments, invoicing, refunds", criteria.get("BILLING").getAsString());
        assertEquals("Bugs, outages, integrations", criteria.get("TECHNICAL").getAsString());
    }

    @Test
    public void anOptionWithoutARubricIsStillSent() {
        JsonObject criteria = Choice.of("Which plan?", Plan.class).toJson().getAsJsonObject().getAsJsonObject("criteria");
        assertTrue(criteria.has("FREE"));
        assertTrue(criteria.get("FREE").isJsonNull());
        assertEquals(2, criteria.size());
    }

    @Test
    public void structuredInstructionsStayStructured() {
        Map<String, Object> instructions = new LinkedHashMap<>();
        instructions.put("question", "Is the resume for the same person as `potential_duplicate`?");
        instructions.put("potential_duplicate", Map.of("name", "John Smith"));
        JsonObject json = Noul.of(instructions).toJson().getAsJsonObject();
        assertEquals("John Smith", json.getAsJsonObject("instructions")
                .getAsJsonObject("potential_duplicate").get("name").getAsString());
    }

    @Test
    public void scoreLevelsKeepTheirOrder() {
        JsonObject json = Score.of("How frustrated is the customer?", "Calm", "Frustrated", "Very angry")
                .toJson().getAsJsonObject();
        assertEquals("score", json.get("type").getAsString());
        assertEquals(3, json.getAsJsonArray("criteria").size());
        assertEquals("Calm", json.getAsJsonArray("criteria").get(0).getAsString());
        assertEquals("Very angry", json.getAsJsonArray("criteria").get(2).getAsString());
    }

    @Test
    public void aQuestionTooWideToAnswerIsRejectedBeforeItCosts() {
        assertThrows(IllegalArgumentException.class, () -> Score.of("How urgent?", "Only one level"));
        assertThrows(IllegalArgumentException.class, () -> Choice.of("Which one?", Map.of()));
    }

}
