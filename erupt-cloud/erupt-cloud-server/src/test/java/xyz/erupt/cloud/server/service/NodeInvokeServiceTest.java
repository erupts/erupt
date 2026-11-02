package xyz.erupt.cloud.server.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.view.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link NodeInvokeService} against a node stubbed at the {@link CloudNodeRouter} seam: one POST per field call,
 * {@code {form, value}} in, the node's R back.
 *
 * @author YuePeng
 */
class NodeInvokeServiceTest {

    @Test
    void aFieldCallIsOnePostAndItsR() throws Exception {
        List<String> paths = new ArrayList<>();
        List<JsonObject> bodies = new ArrayList<>();
        CloudNodeRouter router = new CloudNodeRouter() {
            @Override
            public String proxy(String eruptName, HttpMethod method, String pathTemplate, Object body, Set<CloudSignature.Enforced> enforced) {
                assertEquals(HttpMethod.POST, method);
                paths.add(pathTemplate);
                bodies.add((JsonObject) body);
                return ((JsonObject) body).has("value") ? "{\"success\":true,\"data\":[\"Paris\"]}" : "{\"success\":false,\"message\":\"no\"}";
            }
        };
        NodeInvokeService service = new NodeInvokeService();
        Field field = NodeInvokeService.class.getDeclaredField("cloudNodeRouter");
        field.setAccessible(true);
        field.set(service, router);
        R<JsonElement> cities = service.invoke("blog.Article", NodeInvokeService.Kind.AUTOCOMPLETE, "city",
                JsonParser.parseString("{\"title\":\"x\"}"), "Pa");
        assertEquals("/erupt-api/invoke/{erupt}/autocomplete/city", paths.get(0));
        assertEquals("x", bodies.get(0).getAsJsonObject("form").get("title").getAsString());
        assertEquals("Paris", service.data(cities).getAsJsonArray().get(0).getAsString());
        R<JsonElement> refused = service.invoke("blog.Article", NodeInvokeService.Kind.BUTTON, "calc", null, null);
        assertEquals(0, bodies.get(1).getAsJsonObject("form").size(), "no form is an empty one");
        assertThrows(EruptApiErrorTip.class, () -> service.data(refused));
        service.invoke("blog.Article", NodeInvokeService.Kind.OPERATION_FORM, "publish", new JsonObject());
        assertEquals("/erupt-api/invoke/{erupt}/operation-form/publish", paths.get(2), "an operation goes under its code");
    }

}
