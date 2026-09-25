package xyz.erupt.decision.model;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import xyz.erupt.decision.constant.PrimitiveType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A question defined in the admin has to compile into exactly the same payload as one written
 * in java, or a stored decision would behave differently from the code it replaces.
 *
 * @author YuePeng
 */
public class DecisionQuestionTest {

    private static DecisionQuestion question(PrimitiveType type, String instructions, String criteria) {
        DecisionQuestion question = new DecisionQuestion();
        question.setCode("q");
        question.setType(type);
        question.setInstructions(instructions);
        question.setCriteria(criteria);
        return question;
    }

    @Test
    public void choiceOptionsComeFromTheStoredRubric() {
        JsonObject json = question(PrimitiveType.CHOICE, "Which team should handle this?",
                "{\"billing\":\"Payments, invoicing, refunds\",\"technical\":\"Bugs, outages\"}")
                .toQuestion().toJson().getAsJsonObject();
        assertEquals("choice", json.get("type").getAsString());
        assertEquals("Payments, invoicing, refunds", json.getAsJsonObject("criteria").get("billing").getAsString());
    }

    @Test
    public void scoreLevelsComeFromTheStoredArray() {
        JsonObject json = question(PrimitiveType.SCORE, "How urgent?", "[\"can wait\",\"this week\",\"today\"]")
                .toQuestion().toJson().getAsJsonObject();
        assertEquals(3, json.getAsJsonArray("criteria").size());
        assertEquals("today", json.getAsJsonArray("criteria").get(2).getAsString());
    }

    @Test
    public void aYesNoQuestionNeedsNoRubric() {
        JsonObject json = question(PrimitiveType.NOUL, "Does the customer ask for a refund?", null)
                .toQuestion().toJson().getAsJsonObject();
        assertEquals("noul", json.get("type").getAsString());
        assertTrue(json.get("criteria") == null);
    }

    @Test
    public void instructionsWrittenAsJsonStayStructured() {
        JsonObject json = question(PrimitiveType.NOUL,
                "{\"question\":\"Same person as `duplicate`?\",\"duplicate\":{\"name\":\"John Smith\"}}", null)
                .toQuestion().toJson().getAsJsonObject();
        assertEquals("John Smith", json.getAsJsonObject("instructions")
                .getAsJsonObject("duplicate").get("name").getAsString());
    }

    @Test
    public void prosePassesThroughAsProse() {
        JsonObject json = question(PrimitiveType.NOUL, "Is the customer angry?", null)
                .toQuestion().toJson().getAsJsonObject();
        assertEquals("Is the customer angry?", json.get("instructions").getAsString());
    }

    @Test
    public void theChoiceEditorIsAlreadyTheStoredJson() {
        DecisionQuestion question = question(PrimitiveType.CHOICE, "Which team?", null);
        question.setOptions("{\"billing\":\"Payments and refunds\",\"technical\":\"Bugs and outages\"}");
        question.pack();
        assertEquals("{\"billing\":\"Payments and refunds\",\"technical\":\"Bugs and outages\"}", question.getCriteria());

        question.setOptions(null);
        question.unpack();
        assertEquals(question.getCriteria(), question.getOptions());
        assertNull(question.getLevels());
    }

    @Test
    public void theScoreEditorKeepsTheLevelOrder() {
        DecisionQuestion question = question(PrimitiveType.SCORE, "How urgent?", null);
        question.setLevels("[\"can wait\",\"this week\",\"today\"]");
        question.pack();
        JsonObject json = question.toQuestion().toJson().getAsJsonObject();
        assertEquals("can wait", json.getAsJsonArray("criteria").get(0).getAsString());
        assertEquals("today", json.getAsJsonArray("criteria").get(2).getAsString());
    }

    @Test
    public void theTwoYesNoLinesRoundTripThroughJson() {
        DecisionQuestion question = question(PrimitiveType.NOUL, "Is it urgent?", null);
        question.setTrueMeans("Explicitly time-sensitive");
        question.setFalseMeans("No urgency expressed");
        question.pack();
        assertEquals("{\"true\":\"Explicitly time-sensitive\",\"false\":\"No urgency expressed\"}", question.getCriteria());

        question.setTrueMeans(null);
        question.setFalseMeans(null);
        question.unpack();
        assertEquals("Explicitly time-sensitive", question.getTrueMeans());
        assertEquals("No urgency expressed", question.getFalseMeans());
    }

    @Test
    public void aYesNoQuestionLeftBlankStoresNothing() {
        DecisionQuestion question = question(PrimitiveType.NOUL, "Is it urgent?", null);
        question.pack();
        assertNull(question.getCriteria());
        assertTrue(question.toQuestion().toJson().getAsJsonObject().get("criteria") == null);
    }

    @Test
    public void switchingTypeDropsTheEditorTheOldTypeUsed() {
        DecisionQuestion question = question(PrimitiveType.CHOICE, "Which team?", "{\"billing\":\"…\"}");
        question.setType(PrimitiveType.SCORE);
        question.setLevels("[\"low\",\"high\"]");
        question.pack();
        assertEquals("[\"low\",\"high\"]", question.getCriteria());
        question.unpack();
        assertNull(question.getOptions());
    }

    @Test
    public void aRubricTheTypeCannotUseIsRejectedOnSave() {
        assertThrows(IllegalArgumentException.class,
                () -> question(PrimitiveType.CHOICE, "Which team?", "[\"billing\",\"technical\"]").toQuestion());
        assertThrows(IllegalArgumentException.class,
                () -> question(PrimitiveType.SCORE, "How urgent?", "{\"low\":\"…\"}").toQuestion());
    }

}
