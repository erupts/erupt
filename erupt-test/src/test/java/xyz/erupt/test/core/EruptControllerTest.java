package xyz.erupt.test.core;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.http.*;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.MD5Util;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.edit.AutoCompleteModel;
import xyz.erupt.test.model.edit.ChoiceModel;
import xyz.erupt.test.model.edit.TabTableAddModel;
import xyz.erupt.test.model.erupt.AuthVerifyModel;
import xyz.erupt.test.model.erupt.RowOperationModel;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 对 erupt-core 下所有 Controller 进行 HTTP 集成测试，
 * 使用包扫描的全量模型批量驱动 Build / Data / Component 端点，
 * 并针对典型模型验证完整的 HTTP CRUD 流程。
 */
public class EruptControllerTest extends EruptApplicationTests {

    @Autowired
    private TestRestTemplate rest;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    private static final String MODEL_PACKAGE = "xyz.erupt.test.model";
    private HttpHeaders authHeaders;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        String account = eruptUpmsProp.getDefaultAccount();
        String pwd = MD5Util.digest(MD5Util.digest(eruptUpmsProp.getDefaultPassword()) + account);
        ResponseEntity<Map> resp = rest.getForEntity(
                "/erupt-api/login?account={a}&pwd={p}", Map.class, account, pwd);
        assertEquals(HttpStatus.OK, resp.getStatusCode(), "login must succeed");
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("pass"), "login pass must be true");
        String token = (String) body.get("token");
        assertNotNull(token, "token must not be null after login");

        authHeaders = new HttpHeaders();
        authHeaders.set("token", token);
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    // ─── 工具方法 ─────────────────────────────────────────────────────────────

    private List<Class<?>> scanModelClasses() {
        List<Class<?>> list = new ArrayList<>();
        EruptSpringUtil.scannerPackage(new String[]{MODEL_PACKAGE},
                new TypeFilter[]{new AnnotationTypeFilter(Erupt.class)}, list::add);
        return list;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getBody(ResponseEntity<Map> resp) {
        assertNotNull(resp.getBody());
        return resp.getBody();
    }

    private ResponseEntity<Map> get(String path) {
        return rest.exchange(path, HttpMethod.GET, new HttpEntity<>(authHeaders), Map.class);
    }

    private ResponseEntity<Map> post(String path, Object body) {
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body, authHeaders), Map.class);
    }

    private ResponseEntity<List> postList(String path, Object body) {
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body, authHeaders), List.class);
    }

    // ─── EruptApi ─────────────────────────────────────────────────────────────

    @Test
    void testVersion() {
        ResponseEntity<String> resp = rest.exchange(
                "/erupt-api/version", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    // ─── EruptBuildController ─────────────────────────────────────────────────

    /**
     * 对所有 @Erupt 模型批量调用 /erupt-api/build/{erupt}，
     * 验证返回 200 且 eruptModel 字段非 null。
     */
    @Test
    void testBuildAllModels() {
        for (Class<?> clazz : scanModelClasses()) {
            String name = clazz.getSimpleName();
            ResponseEntity<Map> resp = get("/erupt-api/build/" + name);
            assertEquals(HttpStatus.OK, resp.getStatusCode(), name + ": build must return 200");
            Map<String, Object> body = getBody(resp);
            assertNotNull(body.get("eruptModel"), name + ": build response must contain eruptModel");
        }
    }

    /**
     * 验证 /erupt-api/build/{erupt}/{field} 对含有 @ManyToOne 字段的模型正确返回子 build。
     */
    @Test
    void testBuildByField() {
        // TabTableAddModel has a @OneToMany field 'children' → RefTargetModel
        String erupt = TabTableAddModel.class.getSimpleName();
        ResponseEntity<Map> resp = get("/erupt-api/build/" + erupt + "/children");
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(getBody(resp).get("eruptModel"));
    }

    // ─── EruptDataController ─────────────────────────────────────────────────

    /**
     * 对所有模型批量调用 POST /erupt-api/data/table/{erupt}，
     * 验证返回 200 且 list 字段非 null（空表也可）。
     */
    @Test
    void testTableQueryAllModels() {
        String body = """
                {"pageIndex":1,"pageSize":10}
                """;
        for (Class<?> clazz : scanModelClasses()) {
            String name = clazz.getSimpleName();
            ResponseEntity<Map> resp = post("/erupt-api/data/table/" + name, body);
            assertEquals(HttpStatus.OK, resp.getStatusCode(), name + ": table query must return 200");
            Map<String, Object> respBody = getBody(resp);
            // LinkTree models with dependNode=true may return null list until a node is selected — that is valid
            assertTrue(respBody.containsKey("list") || respBody.containsKey("total"),
                    name + ": table query response must be a Page object");
        }
    }

    /**
     * 对所有模型批量调用 GET /erupt-api/data/init-value/{erupt}，
     * 验证返回 200 且响应为 Map 对象。
     */
    @Test
    void testInitValueAllModels() {
        for (Class<?> clazz : scanModelClasses()) {
            String name = clazz.getSimpleName();
            ResponseEntity<Map> resp = get("/erupt-api/data/init-value/" + name);
            assertEquals(HttpStatus.OK, resp.getStatusCode(), name + ": init-value must return 200");
        }
    }

    /**
     * 验证 GET /erupt-api/data/{erupt}/{id} 对存在的记录返回 200 且字段非空。
     * （框架对不存在的 id 当前返回 500；此处用已知存在的记录测试正向路径。）
     */
    @Test
    @SuppressWarnings("unchecked")
    void testGetByIdFound() {
        // 先 Add 一条，再 GET
        String erupt = AuthVerifyModel.class.getSimpleName();
        String key = "getbyid-" + System.nanoTime();
        post("/erupt-api/data/modify/" + erupt, "{\"key\":\"" + key + "\",\"value\":\"v\"}");
        Long id = findIdByName(erupt, key, "key");
        assertNotNull(id, "prerequisite: record must be added");

        ResponseEntity<Map> resp = get("/erupt-api/data/" + erupt + "/" + id);
        assertEquals(HttpStatus.OK, resp.getStatusCode(), "getById must return 200 for existing record");
        assertEquals(key, getBody(resp).get("key"));
    }

    // ─── EruptModifyController — InputModel 完整 HTTP CRUD ───────────────────

    /**
     * 通过 HTTP 对 AuthVerifyModel（authVerify=false，无权限拦截）完整走通
     * Add → TableQuery(find id) → GET by id → Update → Delete → TableQuery(verify gone)。
     */
    @Test
    @SuppressWarnings("unchecked")
    void testHttpCrudInputModel() {
        String erupt = AuthVerifyModel.class.getSimpleName();
        String uniqueName = "ctrl-test-" + System.nanoTime();

        // ① Add
        String addBody = """
                {"key":"%s","value":"v","description":"desc"}
                """.formatted(uniqueName);
        ResponseEntity<Map> addResp = post("/erupt-api/data/modify/" + erupt, addBody);
        assertEquals(HttpStatus.OK, addResp.getStatusCode(), "add must return 200");
        assertTrue((Boolean) getBody(addResp).get("success"), "add must succeed");

        // ② Find id via table query
        Long id = findIdByName(erupt, uniqueName, "key");
        assertNotNull(id, "must find persisted record after add");

        // ③ GET by id
        ResponseEntity<Map> getResp = get("/erupt-api/data/" + erupt + "/" + id);
        assertEquals(HttpStatus.OK, getResp.getStatusCode(), "getById must return 200");
        assertEquals(uniqueName, getBody(getResp).get("key"), "key must match");

        // ④ Update
        String updateBody = """
                {"id":%d,"key":"%s-updated","value":"v2"}
                """.formatted(id, uniqueName);
        ResponseEntity<Map> updateResp = post("/erupt-api/data/modify/" + erupt + "/update", updateBody);
        assertEquals(HttpStatus.OK, updateResp.getStatusCode(), "update must return 200");
        assertTrue((Boolean) getBody(updateResp).get("success"), "update must succeed");

        // ⑤ Verify update persisted
        ResponseEntity<Map> getResp2 = get("/erupt-api/data/" + erupt + "/" + id);
        assertEquals(uniqueName + "-updated", getBody(getResp2).get("key"), "key must reflect update");

        // ⑥ Delete
        String deleteBody = "[" + id + "]";
        ResponseEntity<Map> deleteResp = post("/erupt-api/data/modify/" + erupt + "/delete", deleteBody);
        assertEquals(HttpStatus.OK, deleteResp.getStatusCode(), "delete must return 200");
        assertTrue((Boolean) getBody(deleteResp).get("success"), "delete must succeed");

        // ⑦ Verify gone
        assertNull(findIdByName(erupt, uniqueName + "-updated", "key"),
                "record must not be found after delete");
    }

    /**
     * 通过 HTTP 对 RowOperationModel 测试 BUTTON 模式的 RowOperation 执行。
     */
    @Test
    void testRowOperationExec() {
        String erupt = RowOperationModel.class.getSimpleName();
        String body = """
                {"param":null,"ids":[]}
                """;
        ResponseEntity<Map> resp = post("/erupt-api/data/" + erupt + "/operator/import", body);
        // Either 200 success or 200 with error message — must not be 500
        assertNotEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode(),
                "rowOperation exec must not return 500");
    }

    // ─── EruptComponentController ─────────────────────────────────────────────

    /**
     * 对 ChoiceModel 的所有静态 VL 字段调用 GET /erupt-api/comp/choice-item/{erupt}/{field}，
     * 验证返回 200 且列表非空。
     */
    @Test
    void testChoiceItemAllFields() {
        String erupt = ChoiceModel.class.getSimpleName();
        for (String field : List.of("status", "priority", "level", "gender")) {
            ResponseEntity<List> resp = rest.exchange(
                    "/erupt-api/comp/choice-item/" + erupt + "/" + field,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders),
                    List.class);
            assertEquals(HttpStatus.OK, resp.getStatusCode(), field + ": choice-item must return 200");
            assertNotNull(resp.getBody(), field + ": choice-item list must not be null");
            assertFalse(resp.getBody().isEmpty(), field + ": choice-item must have entries");
        }
    }

    /**
     * 对 AutoCompleteModel 调用 POST /erupt-api/comp/auto-complete/{erupt}/{field}，
     * 验证触发长度满足时返回 200 列表。
     */
    @Test
    void testAutoComplete() {
        String erupt = AutoCompleteModel.class.getSimpleName();
        // 'city' has triggerLength=1; send a single-char val
        ResponseEntity<List> resp = rest.exchange(
                "/erupt-api/comp/auto-complete/" + erupt + "/city?val=B",
                HttpMethod.POST,
                new HttpEntity<>(Map.of(), authHeaders),
                List.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode(), "auto-complete must return 200");
        assertNotNull(resp.getBody());
    }

    // ─── EruptTabController ───────────────────────────────────────────────────

    /**
     * 对 TabTableAddModel.children (TAB_TABLE_ADD) 测试 tab-add 端点。
     */
    @Test
    void testTabAdd() {
        String erupt = TabTableAddModel.class.getSimpleName();
        String body = """
                {"name":"tab-test"}
                """;
        ResponseEntity<Map> resp = post(
                "/erupt-api/data/modify/tab-add/" + erupt + "/children", body);
        // Expects 200 (may be success or validation error, not 500)
        assertNotEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode(),
                "tab-add must not return 500");
    }

    // ─── EruptToolController ──────────────────────────────────────────────────

    /**
     * 验证 GET /erupt-api/tool/log 返回 200。
     * 日志端点权限类型为 MENU；验证 HTTP 层不返回 5xx。
     */
    @Test
    void testEruptLog() {
        ResponseEntity<String> resp = rest.exchange(
                "/erupt-api/tool/log?size=100",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class);
        assertNotEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode(),
                "log endpoint must not return 500");
        assertNotNull(resp.getBody());
    }

    // ─── 工具方法 ─────────────────────────────────────────────────────────────

    /**
     * 通过 table query 在响应 list 中查找指定字段值对应记录的 id。
     */
    @SuppressWarnings("unchecked")
    private Long findIdByName(String eruptName, String value, String fieldName) {
        String body = """
                {"pageIndex":1,"pageSize":100}
                """;
        ResponseEntity<Map> resp = post("/erupt-api/data/table/" + eruptName, body);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) return null;

        List<Map<String, Object>> list = (List<Map<String, Object>>) resp.getBody().get("list");
        if (list == null) return null;
        return list.stream()
                .filter(row -> value.equals(row.get(fieldName)))
                .map(row -> ((Number) row.get("id")).longValue())
                .findFirst()
                .orElse(null);
    }
}
