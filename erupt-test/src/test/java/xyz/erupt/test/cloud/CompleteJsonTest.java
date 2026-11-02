package xyz.erupt.test.cloud;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.core.proxy.AnnotationProcess;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AnnotationProcess#completeJson}: a node's field JSON with only what it declared gets the defaults a
 * local build carries, while what it declared stays.
 *
 * @author YuePeng
 */
class CompleteJsonTest {

    @Test
    void missingAttributesGetTheirDefaults() {
        JsonObject json = JsonParser.parseString("{\"views\":[{\"title\":\"Progress\"}],"
                + "\"edit\":{\"title\":\"Progress\",\"type\":\"SLIDER\",\"sliderType\":{\"max\":100,\"step\":5}}}").getAsJsonObject();
        JsonObject complete = AnnotationProcess.completeJson(EruptField.class, json);
        JsonObject slider = complete.getAsJsonObject("edit").getAsJsonObject("sliderType");
        assertEquals(100, slider.get("max").getAsInt());
        assertEquals(5, slider.get("step").getAsInt());
        assertTrue(slider.get("markPoints").isJsonArray());
        assertTrue(complete.getAsJsonObject("edit").has("notNull"));
        assertTrue(complete.getAsJsonArray("views").get(0).getAsJsonObject().has("sortable"));
        // the input is not changed
        assertFalse(json.getAsJsonObject("edit").getAsJsonObject("sliderType").has("markPoints"));
    }

    @Test
    void jsonThatDoesNotReadBackIsKept() {
        JsonObject json = JsonParser.parseString("{\"edit\":{\"title\":\"X\",\"type\":\"NO_SUCH_TYPE\"}}").getAsJsonObject();
        assertSame(json, AnnotationProcess.completeJson(EruptField.class, json));
    }

}
