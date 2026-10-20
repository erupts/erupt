package xyz.erupt.cloud.server.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.view.CheckboxModel;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.TreeModel;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link NodeReferenceService} against a node stubbed at the {@link CloudNodeRouter} seam: references sent as
 * keys are resolved once per key, objects are left alone, and a tree is built from the target's pages.
 *
 * @author YuePeng
 */
class NodeReferenceServiceTest {

    private static final String CATEGORY = "{\"fieldName\":\"category\",\"fieldReturnName\":\"Category\",\"eruptFieldJson\":"
            + "{\"views\":[{\"title\":\"Category\",\"column\":\"name\"},{\"title\":\"Code\",\"column\":\"code\"}],"
            + "\"edit\":{\"title\":\"Category\",\"type\":\"REFERENCE_TABLE\",\"referenceTableType\":{\"id\":\"id\",\"label\":\"name\"}}}}";

    private static final String PARENT = "{\"fieldName\":\"parent\",\"fieldReturnName\":\"Category\",\"eruptFieldJson\":"
            + "{\"views\":[{\"title\":\"Parent\",\"column\":\"name\"}],"
            + "\"edit\":{\"title\":\"Parent\",\"type\":\"REFERENCE_TREE\",\"referenceTreeType\":{\"id\":\"id\",\"label\":\"name\",\"pid\":\"parent\"}}}}";

    // the categories under the category picked in the form: dependColumn "parent.id" of a parent sent as its key
    private static final String SUB = "{\"fieldName\":\"sub\",\"fieldReturnName\":\"Category\",\"eruptFieldJson\":"
            + "{\"edit\":{\"title\":\"Sub\",\"type\":\"REFERENCE_TREE\",\"referenceTreeType\":{\"id\":\"id\",\"label\":\"name\","
            + "\"dependField\":\"category\",\"dependColumn\":\"parent.id\"}}}}";

    private static final String FILTERED = "{\"fieldName\":\"filtered\",\"fieldReturnName\":\"Category\",\"eruptFieldJson\":"
            + "{\"edit\":{\"title\":\"Filtered\",\"type\":\"REFERENCE_TREE\",\"filter\":[{\"value\":\"id > 1\"}],"
            + "\"referenceTreeType\":{\"id\":\"id\",\"label\":\"name\"}}}}";

    private static final String TAGS = "{\"fieldName\":\"tags\",\"fieldReturnName\":\"Category\",\"eruptFieldJson\":"
            + "{\"views\":[{\"title\":\"Tags\",\"column\":\"code\"}],\"edit\":{\"title\":\"Tags\",\"type\":\"CHECKBOX\"}}}";

    private static final String SECTIONS = "{\"fieldName\":\"sections\",\"fieldReturnName\":\"Category\",\"eruptFieldJson\":"
            + "{\"edit\":{\"title\":\"Sections\",\"type\":\"TAB_TREE\"}}}";

    private final List<String> calls = new ArrayList<>();

    private final Map<String, JsonObject> categories = new LinkedHashMap<>();

    private NodeReferenceService service;

    private EruptBuildModel build;

    @BeforeAll
    static void context() {
        TestContext.init();
    }

    @BeforeEach
    void setUp() throws Exception {
        category(1, "News", null);
        category(2, "Sport", 1);
        category(3, "Football", 2);
        CloudNodeRouter router = new CloudNodeRouter() {
            @Override
            public EruptBuildModel resolveBuild(String eruptName) {
                assertEquals("blog.Category", eruptName);
                return GsonFactory.getGson().fromJson("{\"eruptModel\":{\"eruptName\":\"Category\",\"eruptJson\":{\"primaryKeyCol\":\"id\","
                        + "\"tree\":{\"id\":\"id\",\"label\":\"name\",\"pid\":\"parent\"}},\"eruptFieldModels\":[]}}", EruptBuildModel.class);
            }

            @Override
            public String proxy(String eruptName, HttpMethod method, String pathTemplate, Object body, Set<CloudSignature.Enforced> enforced) {
                assertEquals("blog.Category", eruptName);
                assertTrue(enforced.contains(CloudSignature.Enforced.POWER));
                calls.add(pathTemplate);
                if (method == HttpMethod.GET) {
                    String id = pathTemplate.substring(pathTemplate.lastIndexOf('/') + 1);
                    if (!categories.containsKey(id)) throw new xyz.erupt.core.exception.EruptWebApiRuntimeException("not found");
                    return categories.get(id).toString();
                }
                JsonObject page = new JsonObject();
                JsonArray list = new JsonArray();
                categories.values().forEach(list::add);
                page.add("list", list);
                return page.toString();
            }
        };
        service = new NodeReferenceService();
        Field field = NodeReferenceService.class.getDeclaredField("cloudNodeRouter");
        field.setAccessible(true);
        field.set(service, router);
        build = GsonFactory.getGson().fromJson("{\"eruptModel\":{\"eruptName\":\"Article\",\"eruptJson\":{},\"eruptFieldModels\":["
                + CATEGORY + "," + PARENT + "," + SUB + "," + FILTERED + "," + TAGS + "," + SECTIONS + "]}}", EruptBuildModel.class);
    }

    private void category(int id, String name, Integer parent) {
        JsonObject row = new JsonObject();
        row.addProperty("id", id);
        row.addProperty("name", name);
        row.addProperty("code", "c" + id);
        row.addProperty("parent", parent);
        categories.put(String.valueOf(id), row);
    }

    @Test
    void keysOfAPageResolveOncePerKey() {
        String page = "{\"list\":[{\"category\":2},{\"category\":2},{\"category\":9},{\"category\":null},{\"category\":{\"id\":\"1\",\"name\":\"News\"}}]}";
        JsonArray list = JsonParser.parseString(service.resolve("blog", build, true, page)).getAsJsonObject().getAsJsonArray("list");
        JsonObject first = list.get(0).getAsJsonObject();
        assertEquals("2", first.getAsJsonObject("category").get("id").getAsString());
        assertEquals("Sport", first.getAsJsonObject("category").get("name").getAsString());
        assertEquals("Sport", first.get("category_name").getAsString());
        assertEquals("c2", first.get("category_code").getAsString());
        // a dangling key keeps the key, without a label
        JsonObject dangling = list.get(2).getAsJsonObject();
        assertEquals("9", dangling.getAsJsonObject("category").get("id").getAsString());
        assertTrue(dangling.get("category_name").isJsonNull());
        // null and an already resolved object stay as they are
        assertTrue(list.get(3).getAsJsonObject().get("category").isJsonNull());
        assertFalse(list.get(4).getAsJsonObject().has("category_name"));
        assertEquals(2, calls.size());
    }

    @Test
    void oneRecordResolves() {
        JsonObject record = JsonParser.parseString(service.resolve("blog", build, false, "{\"category\":\"1\",\"parent\":2}")).getAsJsonObject();
        assertEquals("News", record.get("category_name").getAsString());
        assertEquals("Sport", record.getAsJsonObject("parent").get("name").getAsString());
        assertEquals("Sport", record.get("parent_name").getAsString());
    }

    @Test
    void treeIsBuiltFromTheTargetRecords() {
        List<TreeModel> roots = new ArrayList<>(service.tree("blog", build, "parent", null));
        assertEquals(1, roots.size());
        TreeModel news = roots.get(0);
        assertEquals("News", news.getLabel());
        assertEquals(1, news.getLevel());
        TreeModel sport = news.getChildren().iterator().next();
        assertEquals("Sport", sport.getLabel());
        assertEquals("Football", sport.getChildren().iterator().next().getLabel());
        assertEquals(1, calls.size());
    }

    @Test
    void dependentTreeKeepsTheRecordsUnderTheDependValue() {
        List<TreeModel> nodes = new ArrayList<>(service.tree("blog", build, "sub", "1"));
        assertEquals(1, nodes.size());
        assertEquals("Sport", nodes.get(0).getLabel());
        assertThrows(xyz.erupt.core.exception.EruptWebApiRuntimeException.class, () -> service.tree("blog", build, "sub", null));
    }

    @Test
    void aTreeEruptIsNestedByItsTree() {
        EruptBuildModel category = GsonFactory.getGson().fromJson("{\"eruptModel\":{\"eruptName\":\"Category\",\"eruptJson\":"
                + "{\"tree\":{\"id\":\"id\",\"label\":\"name\",\"pid\":\"parent\"}},\"eruptFieldModels\":[]}}", EruptBuildModel.class);
        List<TreeModel> roots = new ArrayList<>(service.tree("blog", EruptModel.remote("blog.Category", "blog", category)));
        assertEquals(List.of("News"), roots.stream().map(TreeModel::getLabel).toList());
        assertEquals("Football", roots.get(0).getChildren().iterator().next().getChildren().iterator().next().getLabel());
    }

    @Test
    void aLinkTreeIsTheTreeItsFieldPointsAt() {
        List<TreeModel> roots = new ArrayList<>(service.linkTree("blog", build, "parent"));
        assertEquals("Sport", roots.get(0).getChildren().iterator().next().getLabel());
        assertNull(service.linkTree("blog", build, "missing"));
    }

    @Test
    void tabTreeAndCheckboxOptionsAreTheTargetRecords() {
        List<TreeModel> roots = new ArrayList<>(service.tabTree("blog", build, "sections"));
        assertEquals("Sport", roots.get(0).getChildren().iterator().next().getLabel());
        assertNull(service.tabTree("blog", build, "missing"));
        List<CheckboxModel<Object, Object, Object>> options = service.checkbox("blog", build, "tags");
        assertEquals(List.of("1", "2", "3"), options.stream().map(CheckboxModel::getId).toList());
        assertEquals("c2", options.get(1).getLabel(), "labelled by the field's view column");
    }

    @Test
    void thePickerIsTheTargetTableWithItsReferencesResolved() {
        JsonObject page = JsonParser.parseString(service.picker("blog", build, "category",
                "{\"pageIndex\":1,\"pageSize\":10}".getBytes(java.nio.charset.StandardCharsets.UTF_8))).getAsJsonObject();
        assertEquals(3, page.getAsJsonArray("list").size());
        assertEquals("/erupt-api/data/table/{erupt}", calls.get(0));
        assertNull(service.picker("blog", build, "missing", new byte[0]));
    }

    @Test
    void aTreeWithAFilterIsTheNodes() {
        assertTrue(service.nodeBuildsTree(build, "filtered"));
        assertTrue(service.nodeBuildsTree(build, "missing"));
        assertFalse(service.nodeBuildsTree(build, "parent"));
    }

}
