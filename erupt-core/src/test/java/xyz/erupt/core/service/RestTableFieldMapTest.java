package xyz.erupt.core.service;

import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;
import xyz.erupt.core.config.GsonFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure cell-value flattening shared by the SaaS table connectors (Feishu Bitable,
 * DingTalk Notable, Airtable); no network or erupt runtime.
 *
 * @author YuePeng
 */
public class RestTableFieldMapTest {

    private Object value(String json) {
        return EruptRestTableDataService.fieldValue(GsonFactory.getGson().fromJson(json, JsonElement.class));
    }

    @Test
    void scalarsStayClean() {
        assertEquals(8L, value("8"));
        assertEquals(1.5, value("1.5"));
        assertEquals(true, value("true"));
        assertEquals("hello", value("\"hello\""));
        assertNull(value("null"));
    }

    @Test
    void multiSelectBecomesList() {
        Object v = value("[\"a\",\"b\"]");
        assertInstanceOf(List.class, v);
        assertEquals(List.of("a", "b"), v);
    }

    @Test
    void richTextAndPersonJoinToString() {
        // Feishu rich-text segments carry text; person / link carry name / link
        assertEquals("line1line2", value("[{\"type\":\"text\",\"text\":\"line1\"},{\"type\":\"text\",\"text\":\"line2\"}]"));
        assertEquals("Alice", value("[{\"id\":\"u1\",\"name\":\"Alice\"}]"));
        assertEquals("https://erupt.xyz", value("{\"link\":\"https://erupt.xyz\",\"text\":\"https://erupt.xyz\"}"));
    }

    @Test
    void airtableAttachmentAndCollaborator() {
        // attachment objects carry url (plus filename); collaborator carries email / name
        assertEquals("https://dl.airtable.com/a.png", value("[{\"id\":\"att1\",\"url\":\"https://dl.airtable.com/a.png\",\"filename\":\"a.png\"}]"));
        assertEquals("Bob", value("{\"id\":\"usr1\",\"email\":\"bob@example.com\",\"name\":\"Bob\"}"));
        assertEquals(List.of("rec1", "rec2"), value("[\"rec1\",\"rec2\"]"));
    }

    @Test
    void dingtalkUserCarriesUnionIdOnly() {
        // no known display key → raw JSON is kept rather than dropped
        assertEquals("{\"unionId\":\"u1\"}", value("[{\"unionId\":\"u1\"}]"));
    }

}
