package xyz.erupt.notion.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import xyz.erupt.core.config.GsonFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pure Notion property unwrap / wrap mapping, no network or erupt runtime.
 *
 * @author YuePeng
 */
public class NotionPropertyMapTest {

    private Object decode(String json) {
        return EruptNotionDataService.decodeProperty(GsonFactory.getGson().fromJson(json, JsonObject.class));
    }

    private JsonElement json(String value) {
        return GsonFactory.getGson().fromJson(value, JsonElement.class);
    }

    @Test
    void decodeUnwrapsCommonTypes() {
        assertEquals("Hello", decode("{\"type\":\"title\",\"title\":[{\"plain_text\":\"He\"},{\"plain_text\":\"llo\"}]}"));
        assertEquals(5L, decode("{\"type\":\"number\",\"number\":5}"));
        assertEquals("Open", decode("{\"type\":\"select\",\"select\":{\"name\":\"Open\"}}"));
        assertEquals(List.of("a", "b"), decode("{\"type\":\"multi_select\",\"multi_select\":[{\"name\":\"a\"},{\"name\":\"b\"}]}"));
        assertEquals(true, decode("{\"type\":\"checkbox\",\"checkbox\":true}"));
        assertEquals("2026-08-13", decode("{\"type\":\"date\",\"date\":{\"start\":\"2026-08-13\"}}"));
        // unset values flatten to null
        assertNull(decode("{\"type\":\"select\",\"select\":null}"));
    }

    @Test
    void encodeWrapsByType() {
        assertEquals("{\"title\":[{\"text\":{\"content\":\"Hi\"}}]}",
                EruptNotionDataService.encodeProperty("title", json("\"Hi\"")).toString());
        assertEquals("{\"number\":7}", EruptNotionDataService.encodeProperty("number", json("7")).toString());
        // form values arrive as strings; a number field must still emit a JSON number, not a string
        assertEquals("{\"number\":150}", EruptNotionDataService.encodeProperty("number", json("\"150\"")).toString());
        assertEquals("{\"number\":1.5}", EruptNotionDataService.encodeProperty("number", json("\"1.5\"")).toString());
        // blank / non-numeric number input is skipped rather than sent as an invalid value
        assertNull(EruptNotionDataService.encodeProperty("number", json("\"\"")));
        assertNull(EruptNotionDataService.encodeProperty("number", json("\"abc\"")));
        assertEquals("{\"select\":{\"name\":\"Open\"}}", EruptNotionDataService.encodeProperty("select", json("\"Open\"")).toString());
        assertEquals("{\"multi_select\":[{\"name\":\"a\"},{\"name\":\"b\"}]}",
                EruptNotionDataService.encodeProperty("multi_select", json("[\"a\",\"b\"]")).toString());
        assertEquals("{\"checkbox\":true}", EruptNotionDataService.encodeProperty("checkbox", json("true")).toString());
        // computed / unsupported types and nulls are skipped
        assertNull(EruptNotionDataService.encodeProperty("formula", json("\"x\"")));
        assertNull(EruptNotionDataService.encodeProperty("rich_text", json("null")));
    }

}
