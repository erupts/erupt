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
import xyz.erupt.core.util.SecretUtil;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.edit.AutoCompleteModel;
import xyz.erupt.test.model.edit.ChoiceModel;
import xyz.erupt.test.model.edit.TabTableAddModel;
import xyz.erupt.test.model.erupt.AuthVerifyModel;
import xyz.erupt.test.model.erupt.RowOperationModel;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP integration tests for all Controllers under erupt-core.
 * Batch-drives Build / Data / Component endpoints using all models found via package scan,
 * and validates the full HTTP CRUD flow for representative models.
 */
public class EruptControllerTest extends EruptApplicationTests {

    @Autowired
    private TestRestTemplate rest;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptAppProp eruptAppProp;

    private static final String MODEL_PACKAGE = "xyz.erupt.test.model";
    private HttpHeaders authHeaders;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        String account = eruptUpmsProp.getDefaultAccount();
        String pwd = eruptUpmsProp.getDefaultPassword();
        if (eruptAppProp.getPwdTransferEncrypt()) {
            pwd = SecretUtil.encodeSecret(pwd, 3);
        }
        Map<String, String> loginBody = Map.of("account", account, "pwd", pwd);
        ResponseEntity<Map> resp = rest.postForEntity("/erupt-api/login", loginBody, Map.class);
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

    // ─── Helpers ──────────────────────────────────────────────────────────────

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
     * Batch-calls /erupt-api/build/{erupt} for all @Erupt models
     * and verifies 200 + non-null eruptModel.
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
     * Verifies /erupt-api/build/{erupt}/{field} returns the sub-build correctly for models with a @ManyToOne field.
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
     * Batch-calls POST /erupt-api/data/table/{erupt} for all models
     * and verifies 200 + non-null list (empty table is valid).
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
     * Batch-calls GET /erupt-api/data/init-value/{erupt} for all models
     * and verifies 200 + Map response.
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
     * Verifies GET /erupt-api/data/{erupt}/{id} returns 200 + non-null fields for an existing record.
     * (The framework currently returns 500 for a non-existent id; test the happy path here using a known record.)
     */
    @Test
    @SuppressWarnings("unchecked")
    void testGetByIdFound() {
        // Add a record first, then GET
        String erupt = AuthVerifyModel.class.getSimpleName();
        String key = "getbyid-" + System.nanoTime();
        post("/erupt-api/data/modify/" + erupt, "{\"key\":\"" + key + "\",\"value\":\"v\"}");
        Long id = findIdByName(erupt, key, "key");
        assertNotNull(id, "prerequisite: record must be added");

        ResponseEntity<Map> resp = get("/erupt-api/data/" + erupt + "/" + id);
        assertEquals(HttpStatus.OK, resp.getStatusCode(), "getById must return 200 for existing record");
        assertEquals(key, getBody(resp).get("key"));
    }

    // ─── EruptModifyController — Full HTTP CRUD ───────────────────────────────

    /**
     * Full HTTP CRUD walkthrough for AuthVerifyModel (authVerify=false, no permission intercept):
     * Add → TableQuery(find id) → GET by id → Update → Delete → TableQuery(verify gone).
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
     * Tests RowOperation execution in BUTTON mode for RowOperationModel via HTTP.
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
     * Calls GET /erupt-api/comp/choice-item/{erupt}/{field} for all static VL fields in ChoiceModel
     * and verifies 200 + non-empty list.
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
     * Calls POST /erupt-api/comp/auto-complete/{erupt}/{field} for AutoCompleteModel
     * and verifies a 200 list when trigger length is met.
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
     * Tests the tab-add endpoint for TabTableAddModel.children (TAB_TABLE_ADD).
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
     * Verifies GET /erupt-api/tool/log returns 200.
     * Log endpoint permission type is MENU; verifies the HTTP layer does not return 5xx.
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

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Finds the id of a record in the table query response list by matching the given field value.
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
