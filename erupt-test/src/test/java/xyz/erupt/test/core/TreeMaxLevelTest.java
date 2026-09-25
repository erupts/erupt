package xyz.erupt.test.core;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import xyz.erupt.core.util.SecretUtil;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.erupt.MaxLevelTreeModel;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code @Tree(maxLevel = 2)} is enforced on the server for every way a node can be placed: added
 * under a parent, moved with its subtree through a form update, or re-parented through a cell edit.
 * The UI merely hides the button; these paths are what an import or a direct API call would take.
 */
public class TreeMaxLevelTest extends EruptApplicationTests {

    private static final String ERUPT = MaxLevelTreeModel.class.getSimpleName();

    @Autowired
    private TestRestTemplate rest;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptAppProp eruptAppProp;

    private HttpHeaders headers;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void login() {
        String pwd = eruptUpmsProp.getDefaultPassword();
        if (eruptAppProp.getPwdTransferEncrypt()) pwd = SecretUtil.encodeSecret(pwd, 3);
        ResponseEntity<Map> resp = rest.postForEntity("/erupt-api/login",
                Map.of("account", eruptUpmsProp.getDefaultAccount(), "pwd", pwd), Map.class);
        assertTrue((Boolean) resp.getBody().get("pass"), "login must succeed");
        headers = new HttpHeaders();
        headers.set("token", (String) resp.getBody().get("token"));
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void aNodeCannotBeAddedBelowTheLimit() {
        long root = this.add("root", null);
        long child = this.add("child", root);
        Map<String, Object> refused = this.post(ERUPT, "{\"name\":\"grandchild\",\"parentId\":" + child + "}");
        assertFalse((Boolean) refused.get("success"), "level 3 must be refused on a 2 level tree");
        assertTrue(((String) refused.get("message")).contains("2"), "the message names the limit: " + refused.get("message"));
        assertNull(this.findId("grandchild"), "nothing may be stored for a refused add");
    }

    @Test
    void aSubtreeCannotBeMovedBelowTheLimit() {
        long rootA = this.add("rootA", null);
        this.add("childA", rootA);
        long rootB = this.add("rootB", null);
        long childB = this.add("childB", rootB);

        // rootA carries a child: under rootB it would put childA on level 3
        Map<String, Object> moveWithSubtree = this.post(ERUPT + "/update",
                "{\"id\":" + rootA + ",\"name\":\"rootA\",\"parentId\":" + rootB + "}");
        assertFalse((Boolean) moveWithSubtree.get("success"), "a move must count the subtree it drags along");

        // a leaf under a level 2 node would itself sit on level 3
        Map<String, Object> moveLeaf = this.post(ERUPT + "/update",
                "{\"id\":" + childB + ",\"name\":\"childB\",\"parentId\":" + this.findId("childA") + "}");
        assertFalse((Boolean) moveLeaf.get("success"));

        // the same leaf may still move between roots, that stays on level 2
        Map<String, Object> moveAcross = this.post(ERUPT + "/update",
                "{\"id\":" + childB + ",\"name\":\"childB\",\"parentId\":" + rootA + "}");
        assertTrue((Boolean) moveAcross.get("success"), "a legal move must still pass: " + moveAcross.get("message"));
    }

    @Test
    void aCellEditCannotReparentBelowTheLimit() {
        long root = this.add("cellRoot", null);
        long child = this.add("cellChild", root);
        long other = this.add("cellOther", null);
        Map<String, Object> refused = this.post(ERUPT + "/update-cell",
                "{\"id\":" + other + ",\"field\":\"parentId\",\"value\":" + child + "}");
        assertFalse((Boolean) refused.get("success"), "a cell edit is a save like any other");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private long add(String name, Long parentId) {
        Map<String, Object> resp = this.post(ERUPT, "{\"name\":\"" + name + "\",\"parentId\":" + parentId + "}");
        assertTrue((Boolean) resp.get("success"), "adding " + name + " must pass: " + resp.get("message"));
        Long id = this.findId(name);
        assertNotNull(id);
        return id;
    }

    private Long findId(String name) {
        return eruptDao.lambdaQuery(MaxLevelTreeModel.class).eq(MaxLevelTreeModel::getName, name).list()
                .stream().map(MaxLevelTreeModel::getId).findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String path, String body) {
        ResponseEntity<Map> resp = rest.exchange("/erupt-api/data/modify/" + path, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        assertNotNull(resp.getBody(), "the modify API answers with a body");
        return resp.getBody();
    }

}
