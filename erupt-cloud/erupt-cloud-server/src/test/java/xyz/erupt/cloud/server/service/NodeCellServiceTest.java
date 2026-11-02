package xyz.erupt.cloud.server.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.R;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link NodeCellService#dragSort} against a node stubbed at the {@link CloudNodeRouter} seam: each row is read,
 * its dragSort field set to the new position and the whole row saved through the node's update.
 *
 * @author YuePeng
 */
class NodeCellServiceTest {

    private final Map<String, JsonObject> rows = new LinkedHashMap<>();

    private final List<JsonObject> saved = new ArrayList<>();

    private NodeCellService service;

    @BeforeAll
    static void context() {
        TestContext.init();
    }

    @BeforeEach
    void setUp() throws Exception {
        for (int id = 1; id <= 3; id++) {
            JsonObject row = new JsonObject();
            row.addProperty("id", id);
            row.addProperty("title", "t" + id);
            row.addProperty("sort", id);
            rows.put(String.valueOf(id), row);
        }
        CloudNodeRouter router = new CloudNodeRouter() {
            @Override
            public String proxy(String eruptName, HttpMethod method, String pathTemplate, Object body, Set<CloudSignature.Enforced> enforced) {
                assertEquals("blog.Article", eruptName);
                if (method == HttpMethod.GET) {
                    assertEquals(EnumSet.of(CloudSignature.Enforced.POWER), enforced);
                    return Optional.ofNullable(rows.get(pathTemplate.substring(pathTemplate.lastIndexOf('/') + 1)))
                            .map(JsonObject::toString).orElse("{}");
                }
                assertEquals(EnumSet.of(CloudSignature.Enforced.POWER, CloudSignature.Enforced.RECORD), enforced);
                saved.add((JsonObject) body);
                return GsonFactory.getGson().toJson(R.ok());
            }
        };
        service = new NodeCellService();
        Field field = NodeCellService.class.getDeclaredField("cloudNodeRouter");
        field.setAccessible(true);
        field.set(service, router);
    }

    private static EruptBuildModel build(String dragSort) {
        return GsonFactory.getGson().fromJson("{\"eruptModel\":{\"eruptName\":\"Article\",\"eruptJson\":{\"primaryKeyCol\":\"id\""
                + (null == dragSort ? "" : ",\"dragSort\":{\"field\":\"" + dragSort + "\"}") + "},\"eruptFieldModels\":["
                + "{\"fieldName\":\"title\",\"eruptFieldJson\":{\"edit\":{\"title\":\"Title\",\"type\":\"INPUT\"}}},"
                + "{\"fieldName\":\"sort\",\"eruptFieldJson\":{\"edit\":{\"title\":\"Sort\",\"type\":\"NUMBER\"}}}]}}", EruptBuildModel.class);
    }

    private static byte[] body(String json) {
        return json.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void eachRowIsSavedWithItsNewPosition() {
        R<Void> result = service.dragSort("blog.Article", build("sort"), body("{\"3\":1,\"1\":2,\"2\":3}"));
        assertTrue(result.isSuccess(), result.getMessage());
        assertEquals(3, saved.size());
        assertEquals("3", saved.get(0).get("id").getAsString());
        assertEquals(1, saved.get(0).get("sort").getAsInt());
        // the whole row goes back, not only the position
        assertEquals("t3", saved.get(0).get("title").getAsString());
        assertEquals(JsonParser.parseString("2"), saved.get(1).get("sort"));
    }

    @Test
    void aMissingRowStopsTheSort() {
        R<Void> result = service.dragSort("blog.Article", build("sort"), body("{\"1\":2,\"9\":1,\"2\":3}"));
        assertFalse(result.isSuccess());
        assertEquals(1, saved.size());
    }

    @Test
    void anEruptWithoutDragSortIsRefused() {
        assertFalse(service.dragSort("blog.Article", build(null), body("{\"1\":2}")).isSuccess());
        assertTrue(saved.isEmpty());
    }

}
