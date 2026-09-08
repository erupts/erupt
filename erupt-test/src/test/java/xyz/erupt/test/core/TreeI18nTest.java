package xyz.erupt.test.core;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import xyz.erupt.core.i18n.I18nRunner;
import xyz.erupt.core.util.SecretUtil;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.erupt.I18nTreeModel;
import xyz.erupt.test.model.erupt.TreeModel;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tree data i18n: labels of a tree entity are translated only when the entity carries @EruptI18n.
 * Node names reuse the CSV keys "Y" / "N", which every shipped language translates.
 */
public class TreeI18nTest extends EruptApplicationTests {

    private static final String TREE_API = "/erupt-api/data/tree/";

    @Autowired
    private TestRestTemplate rest;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptAppProp eruptAppProp;

    private String token;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        String pwd = eruptUpmsProp.getDefaultPassword();
        if (eruptAppProp.getPwdTransferEncrypt()) {
            pwd = SecretUtil.encodeSecret(pwd, 3);
        }
        ResponseEntity<Map> resp = rest.postForEntity("/erupt-api/login",
                Map.of("account", eruptUpmsProp.getDefaultAccount(), "pwd", pwd), Map.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode(), "login must succeed");
        token = (String) resp.getBody().get("token");
        assertNotNull(token);

        if (eruptDao.lambdaQuery(I18nTreeModel.class).count() == 0) {
            I18nTreeModel root = new I18nTreeModel();
            root.setName("Y");
            eruptDao.persistAndFlush(root);
            I18nTreeModel child = new I18nTreeModel();
            child.setName("N");
            child.setParentId(root.getId());
            eruptDao.persistAndFlush(child);
        }
        if (eruptDao.lambdaQuery(TreeModel.class).eq(TreeModel::getName, "Y").count() == 0) {
            TreeModel plain = new TreeModel();
            plain.setName("Y");
            eruptDao.persistAndFlush(plain);
        }
    }

    private List<Map<String, Object>> tree(Class<?> erupt, String lang) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);
        headers.set("erupt", erupt.getSimpleName());
        if (null != lang) headers.set("Lang", lang);
        ResponseEntity<List<Map<String, Object>>> resp = rest.exchange(TREE_API + erupt.getSimpleName(),
                HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});
        assertEquals(HttpStatus.OK, resp.getStatusCode(), erupt.getSimpleName() + ": tree must return 200");
        assertNotNull(resp.getBody());
        return resp.getBody();
    }

    private Map<String, Object> rootNode(List<Map<String, Object>> nodes) {
        return nodes.stream().filter(n -> n.get("pid") == null).findFirst().orElseThrow();
    }

    /** @EruptI18n entity: label follows the Lang header, child nodes included. */
    @Test
    @SuppressWarnings("unchecked")
    void testAnnotatedTreeLabelsTranslated() {
        Map<String, Object> zhRoot = rootNode(tree(I18nTreeModel.class, "zh-CN"));
        assertEquals(I18nRunner.getI18nValue("zh-CN", "Y"), zhRoot.get("label"));
        assertNotEquals("Y", zhRoot.get("label"), "zh-CN label must not be the raw key");
        List<Map<String, Object>> children = (List<Map<String, Object>>) zhRoot.get("children");
        assertNotNull(children, "child node must be attached to the root");
        assertEquals(I18nRunner.getI18nValue("zh-CN", "N"), children.get(0).get("label"));

        Map<String, Object> enRoot = rootNode(tree(I18nTreeModel.class, "en-US"));
        assertEquals("yes", enRoot.get("label"), "en-US must resolve the CSV value of 'Y'");
    }

    /** No Lang header: default locale applies, so the label is still resolved through the CSV. */
    @Test
    void testAnnotatedTreeUsesDefaultLocaleWithoutHeader() {
        Map<String, Object> root = rootNode(tree(I18nTreeModel.class, null));
        assertEquals(I18nRunner.getI18nValue("en-US", "Y"), root.get("label"));
    }

    /** Entity without @EruptI18n: labels are returned verbatim regardless of Lang. */
    @Test
    void testPlainTreeLabelsUntouched() {
        List<Map<String, Object>> nodes = tree(TreeModel.class, "zh-CN");
        assertTrue(nodes.stream().anyMatch(n -> "Y".equals(n.get("label"))),
                "tree without @EruptI18n must keep the raw label");
    }
}
