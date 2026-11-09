package xyz.erupt.cloud.server.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptReqHeader;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.view.EruptBuildModel;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link NodeDrillService}: a drill as the condition a v2 node gets (PROTOCOL.md §12.4), with the node stubbed at
 * the {@link CloudNodeRouter} seam.
 *
 * @author YuePeng
 */
class NodeDrillServiceTest {

    private static final EruptBuildModel CATEGORY = GsonFactory.getGson().fromJson("""
            {"eruptModel": {"eruptName": "Category", "eruptJson": {"primaryKeyCol": "id",
              "drills": [{"code": "articles", "title": "Articles",
                "link": {"linkErupt": "Article", "column": "code", "joinColumn": "category"}}]}}}""", EruptBuildModel.class);

    private NodeDrillService service;

    private String row;

    @BeforeAll
    static void context() {
        TestContext.init();
    }

    @BeforeEach
    void setUp() throws Exception {
        row = "{\"id\": 3, \"code\": \"news\"}";
        CloudNodeRouter router = new CloudNodeRouter() {
            @Override
            public EruptBuildModel resolveBuild(String eruptName) {
                assertEquals("blog.Category", eruptName);
                return CATEGORY;
            }

            @Override
            public String proxy(String eruptName, HttpMethod method, String pathTemplate, Object body, Set<CloudSignature.Enforced> enforced) {
                assertEquals("blog.Category", eruptName);
                assertEquals(HttpMethod.GET, method);
                assertTrue(pathTemplate.endsWith("/3"), pathTemplate);
                assertEquals(EnumSet.of(CloudSignature.Enforced.POWER), enforced);
                return row;
            }
        };
        service = new NodeDrillService();
        Field field = NodeDrillService.class.getDeclaredField("cloudNodeRouter");
        field.setAccessible(true);
        field.set(service, router);
    }

    private static MockHttpServletRequest drill(String code, String source) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(EruptReqHeader.DRILL, code);
        request.addHeader(EruptReqHeader.DRILL_VALUE, "3");
        request.addHeader(EruptReqHeader.DRILL_SOURCE_ERUPT, source);
        return request;
    }

    private JsonObject apply(MockHttpServletRequest request) {
        return service.apply("blog", "blog.Article", request, JsonParser.parseString("{\"pageIndex\": 1}").getAsJsonObject());
    }

    @Test
    void aDrillIsTheSourceRowsColumnOnTheJoinColumn() {
        JsonObject query = apply(drill("articles", "blog.Category"));
        assertEquals(JsonParser.parseString("""
                [{"key": "category", "expression": "EQ", "value": "news"}]"""), query.get("condition"));
        assertEquals(JsonParser.parseString("{\"erupt\": \"Category\", \"code\": \"articles\"}"), query.get(NodeDrillService.DRILL));
        assertEquals(1, query.get("pageIndex").getAsInt());
    }

    @Test
    void aJoinColumnThroughTheReferenceIsTheReferenceField() {
        // Java writes "category.id"; a node stores the reference as the key, under "category"
        CATEGORY.getEruptModel().getEruptJson().getAsJsonArray("drills").get(0).getAsJsonObject()
                .getAsJsonObject("link").addProperty("joinColumn", "category.id");
        try {
            assertEquals(JsonParser.parseString("""
                    [{"key": "category", "expression": "EQ", "value": "news"}]"""), apply(drill("articles", "blog.Category")).get("condition"));
        } finally {
            CATEGORY.getEruptModel().getEruptJson().getAsJsonArray("drills").get(0).getAsJsonObject()
                    .getAsJsonObject("link").addProperty("joinColumn", "category");
        }
    }

    @Test
    void aSourceRowWithoutTheValueLinksToRowsWithoutOne() {
        row = "{\"id\": 3}";
        assertEquals(JsonParser.parseString("""
                [{"key": "category", "expression": "NULL"}]"""), apply(drill("articles", "blog.Category")).get("condition"));
    }

    @Test
    void withoutDrillHeadersTheQueryIsUntouched() {
        JsonObject query = JsonParser.parseString("{\"pageIndex\": 1}").getAsJsonObject();
        assertSame(query, service.apply("blog", "blog.Article", new MockHttpServletRequest(), query));
    }

    @Test
    void aDrillTheBrowserWritesIntoTheBodyIsDropped() {
        // without the headers no drill applies, so a drill member would pick a linkCondition without its condition
        JsonObject forged = JsonParser.parseString("{\"pageIndex\": 1, \"drill\": {\"erupt\": \"Category\", \"code\": \"articles\"}}").getAsJsonObject();
        JsonObject query = service.apply("blog", "blog.Article", new MockHttpServletRequest(), forged);
        assertFalse(query.has(NodeDrillService.DRILL));
        assertTrue(forged.has(NodeDrillService.DRILL), "the request's own body is not changed");
        // with them, the server's drill replaces the forged one
        forged.add(NodeDrillService.DRILL, JsonParser.parseString("{\"erupt\": \"Other\", \"code\": \"x\"}"));
        assertEquals(JsonParser.parseString("{\"erupt\": \"Category\", \"code\": \"articles\"}"),
                service.apply("blog", "blog.Article", drill("articles", "blog.Category"), forged).get(NodeDrillService.DRILL));
    }

    @Test
    void aSourceRowTheNodeDoesNotHaveIsRefused() {
        // not the rows without a value: a drill from a missing row lists nothing
        for (String missing : new String[]{"null", "{}"}) {
            row = missing;
            assertThrows(EruptWebApiRuntimeException.class, () -> apply(drill("articles", "blog.Category")), missing);
        }
    }

    @Test
    void aDrillTheSourceDoesNotDeclareOrThatLeadsElsewhereIsRefused() {
        // EruptNoLegalPowerException answers as the permission error it builds while constructed
        assertThrows(EruptWebApiRuntimeException.class, () -> apply(drill("authors", "blog.Category")));
        assertThrows(EruptWebApiRuntimeException.class, () -> apply(drill("articles", "shop.Category")));
        assertThrows(EruptWebApiRuntimeException.class, () ->
                service.apply("blog", "blog.Comment", drill("articles", "blog.Category"), new JsonObject()));
    }

}
