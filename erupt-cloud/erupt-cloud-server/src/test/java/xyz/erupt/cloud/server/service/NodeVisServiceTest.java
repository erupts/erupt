package xyz.erupt.cloud.server.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.core.annotation.EruptRouter.Resolve;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.R;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link NodeVisService} against a node stubbed at the {@link CloudNodeRouter} seam: board, calendar and gantt
 * writes are rows read through the node's get, given the dragged values and saved whole through its update.
 *
 * @author YuePeng
 */
class NodeVisServiceTest {

    private static final String VIS = "[{\"code\":\"board\",\"title\":\"Board\",\"type\":\"BOARD\",\"boardView\":{\"groupField\":\"status\",\"swimlaneField\":\"owner\"}},"
            + "{\"code\":\"cal\",\"title\":\"Calendar\",\"type\":\"CALENDAR\",\"calendarView\":{\"dateField\":\"start\",\"endDateField\":\"end\"}},"
            + "{\"code\":\"gantt\",\"title\":\"Gantt\",\"type\":\"GANTT\",\"ganttView\":{\"startDateField\":\"start\",\"endDateField\":\"end\",\"dependencyField\":\"after\"}}]";

    private final Map<String, JsonObject> rows = new LinkedHashMap<>();

    private NodeVisService service;

    private EruptBuildModel build;

    @BeforeAll
    static void context() {
        TestContext.init();
    }

    @BeforeEach
    void setUp() throws Exception {
        for (int id = 1; id <= 3; id++) {
            JsonObject row = new JsonObject();
            row.addProperty("id", id);
            row.addProperty("status", "todo");
            row.addProperty("owner", "a");
            row.addProperty("start", "2026-10-01");
            row.addProperty("end", "2026-10-02");
            row.add("after", new JsonArray());
            rows.put(String.valueOf(id), row);
        }
        CloudNodeRouter router = new CloudNodeRouter() {
            @Override
            public String proxy(String eruptName, HttpMethod method, String pathTemplate, Object body, Set<CloudSignature.Enforced> enforced) {
                if (method == HttpMethod.GET) {
                    return Optional.ofNullable(rows.get(pathTemplate.substring(pathTemplate.lastIndexOf('/') + 1)))
                            .map(JsonObject::deepCopy).map(JsonObject::toString).orElse("{}");
                }
                JsonObject record = (JsonObject) body;
                rows.put(record.get("id").getAsString(), record);
                return GsonFactory.getGson().toJson(R.ok());
            }
        };
        NodeCellService cells = new NodeCellService();
        set(cells, "cloudNodeRouter", router);
        service = new NodeVisService();
        set(service, "nodeCellService", cells);
        StringBuilder fields = new StringBuilder();
        for (String name : List.of("status", "owner", "start", "end")) {
            if (!fields.isEmpty()) fields.append(',');
            fields.append("{\"fieldName\":\"").append(name).append("\",\"eruptFieldJson\":{\"edit\":{\"title\":\"").append(name)
                    .append("\",\"type\":\"INPUT\"}}}");
        }
        build = GsonFactory.getGson().fromJson("{\"eruptModel\":{\"eruptName\":\"Task\",\"eruptJson\":{\"primaryKeyCol\":\"id\",\"vis\":"
                + VIS + "},\"eruptFieldModels\":[" + fields + "]}}", EruptBuildModel.class);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private R<?> answer(Resolve resolve, String body) {
        return service.answer(resolve, "blog.Task", build, body.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void aCardMovesToItsGroupAndSwimlane() {
        assertTrue(answer(Resolve.VIS_BOARD_GROUP, "{\"visCode\":\"board\",\"pk\":1,\"groupValue\":\"done\"}").isSuccess());
        assertEquals("done", rows.get("1").get("status").getAsString());
        assertEquals("a", rows.get("1").get("owner").getAsString());
        answer(Resolve.VIS_BOARD_GROUP, "{\"visCode\":\"board\",\"pk\":1,\"groupValue\":\"todo\",\"swimlaneValue\":\"b\",\"swimlaneChanged\":true}");
        assertEquals("b", rows.get("1").get("owner").getAsString());
    }

    @Test
    void calendarAndGanttMoveTheDates() {
        answer(Resolve.VIS_CALENDAR_DATE, "{\"visCode\":\"cal\",\"pk\":2,\"date\":\"2026-11-01\"}");
        assertEquals("2026-11-01", rows.get("2").get("start").getAsString());
        assertEquals("2026-10-02", rows.get("2").get("end").getAsString(), "an end date not sent stays");
        answer(Resolve.VIS_GANTT_DATE, "{\"visCode\":\"gantt\",\"pk\":2,\"startDate\":\"2026-12-01\",\"endDate\":\"2026-12-09\"}");
        assertEquals("2026-12-09", rows.get("2").get("end").getAsString());
        assertThrows(EruptWebApiRuntimeException.class, () -> answer(Resolve.VIS_GANTT_DATE, "{\"visCode\":\"none\",\"pk\":2}"));
    }

    @Test
    void ganttLinksAreAddedReadAndRefusedInACycle() {
        assertTrue(answer(Resolve.VIS_GANTT_LINK, "{\"visCode\":\"gantt\",\"pk\":2,\"predecessorPk\":1}").isSuccess());
        assertTrue(answer(Resolve.VIS_GANTT_LINK, "{\"visCode\":\"gantt\",\"pk\":3,\"predecessorPk\":2}").isSuccess());
        // 1 → 2 → 3: 3 before 1 would close the loop
        assertFalse(answer(Resolve.VIS_GANTT_LINK, "{\"visCode\":\"gantt\",\"pk\":1,\"predecessorPk\":3}").isSuccess());
        assertFalse(answer(Resolve.VIS_GANTT_LINK, "{\"visCode\":\"gantt\",\"pk\":1,\"predecessorPk\":1}").isSuccess());
        R<?> links = answer(Resolve.VIS_GANTT_LINKS, "{\"visCode\":\"gantt\",\"pks\":[1,2,3]}");
        assertEquals(Map.of("1", List.of(), "2", List.of("1"), "3", List.of("2")), links.getData());
        answer(Resolve.VIS_GANTT_LINK, "{\"visCode\":\"gantt\",\"pk\":3,\"predecessorPk\":2,\"remove\":true}");
        assertEquals(0, rows.get("3").getAsJsonArray("after").size());
    }

}
