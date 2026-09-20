package xyz.erupt.test.core;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import xyz.erupt.core.util.SecretUtil;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.edit.CombineModel;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A COMBINE field embeds a child form, and its @View(column = ...) entries project fields of
 * the embedded object into the parent table. Gitee IKGOKN: every such column has to reach the
 * table rows under the key the frontend reads it from ("field_column").
 */
public class CombineViewTest extends EruptApplicationTests {

    @Autowired
    private TestRestTemplate rest;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptAppProp eruptAppProp;

    private HttpHeaders authHeaders;

    @BeforeEach
    void login() {
        String pwd = eruptUpmsProp.getDefaultPassword();
        if (eruptAppProp.getPwdTransferEncrypt()) {
            pwd = SecretUtil.encodeSecret(pwd, 3);
        }
        Map<String, Object> body = rest.postForEntity("/erupt-api/login",
                Map.of("account", eruptUpmsProp.getDefaultAccount(), "pwd", pwd), Map.class).getBody();
        assertNotNull(body);
        authHeaders = new HttpHeaders();
        authHeaders.set("token", (String) body.get("token"));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @SuppressWarnings("unchecked")
    void combineViewColumnsReachTheTable() {
        String erupt = CombineModel.class.getSimpleName();
        String name = "combine-" + System.nanoTime();
        ResponseEntity<Map> add = rest.exchange("/erupt-api/data/modify/" + erupt, HttpMethod.POST,
                new HttpEntity<>("""
                        {"detail":{"name":"%s","description":"combine-desc"}}
                        """.formatted(name), authHeaders), Map.class);
        assertEquals(HttpStatus.OK, add.getStatusCode());
        assertTrue((Boolean) add.getBody().get("success"), String.valueOf(add.getBody()));

        ResponseEntity<Map> table = rest.exchange("/erupt-api/data/table/" + erupt, HttpMethod.POST,
                new HttpEntity<>("{\"pageIndex\":1,\"pageSize\":100}", authHeaders), Map.class);
        assertEquals(HttpStatus.OK, table.getStatusCode());
        List<Map<String, Object>> rows = (List<Map<String, Object>>) table.getBody().get("list");
        Map<String, Object> row = rows.stream().filter(r -> name.equals(r.get("detail_name"))).findFirst()
                .orElseGet(() -> fail("row not found by detail_name, rows: " + rows));
        assertEquals("combine-desc", row.get("detail_description"));
    }
}
