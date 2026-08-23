package xyz.erupt.feishu.service;

import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;
import xyz.erupt.core.config.GsonFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure Bitable field-value mapping, no network or erupt runtime.
 *
 * @author YuePeng
 */
public class FeishuFieldMapTest {

    private Object value(String json) {
        return EruptFeishuDataService.fieldValue(GsonFactory.getGson().fromJson(json, JsonElement.class));
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
        // rich-text segments carry text; person / link carry name / link
        assertEquals("line1line2", value("[{\"type\":\"text\",\"text\":\"line1\"},{\"type\":\"text\",\"text\":\"line2\"}]"));
        assertEquals("Alice", value("[{\"id\":\"u1\",\"name\":\"Alice\"}]"));
        assertEquals("https://erupt.xyz", value("{\"link\":\"https://erupt.xyz\",\"text\":\"https://erupt.xyz\"}"));
    }

}
