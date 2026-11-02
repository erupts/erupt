package xyz.erupt.http;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Envelope unwrapping by dotted path, no network or erupt runtime (so failures are
 * asserted as plain RuntimeException: the translated message needs the Spring context).
 *
 * @author YuePeng
 */
public class HttpEnvelopeTest {

    private static final String WRAPPED = "{\"code\":0,\"data\":{\"items\":[{\"id\":1},{\"id\":2}],\"meta\":{\"count\":42}}}";

    @Test
    void emptyPathAcceptsArrayOrListProperty() {
        assertEquals(2, HttpEnvelope.list("[{\"id\":1},{\"id\":2}]", "").size());
        assertEquals(1, HttpEnvelope.list("{\"total\":9,\"list\":[{\"id\":1}]}", "").size());
        assertEquals(9L, HttpEnvelope.total("{\"total\":9,\"list\":[]}", ""));
        assertNull(HttpEnvelope.total("[{\"id\":1}]", ""));
    }

    @Test
    void dottedPathWalksNestedEnvelope() {
        List<Map<String, Object>> list = HttpEnvelope.list(WRAPPED, "data.items");
        assertEquals(2, list.size());
        assertEquals(42L, HttpEnvelope.total(WRAPPED, "data.meta.count"));
        assertNull(HttpEnvelope.total(WRAPPED, "data.meta.missing"));
    }

    @Test
    void itemUnwrapsToModelClass() {
        Item item = HttpEnvelope.item("{\"data\":{\"id\":7,\"name\":\"x\"}}", "data", Item.class);
        assertEquals(7L, item.id);
        assertEquals("x", item.name);
        assertEquals(7L, HttpEnvelope.item("{\"id\":7}", "", Item.class).id);
    }

    @Test
    void unexpectedShapesFail() {
        assertThrows(RuntimeException.class, () -> HttpEnvelope.list("{\"foo\":1}", ""));
        assertThrows(RuntimeException.class, () -> HttpEnvelope.list(WRAPPED, "data.meta"));
        assertThrows(RuntimeException.class, () -> HttpEnvelope.list("not json", ""));
    }

    @Test
    void messagePicksFirstKnownKey() {
        assertEquals("Bad credentials", HttpEnvelope.message("{\"message\":\"Bad credentials\",\"doc\":\"...\"}"));
        assertEquals("invalid_grant", HttpEnvelope.message("{\"error\":\"invalid_grant\"}"));
        assertNull(HttpEnvelope.message("<html>502</html>"));
        assertNull(HttpEnvelope.message(""));
    }

    static class Item {
        Long id;
        String name;
    }

}
