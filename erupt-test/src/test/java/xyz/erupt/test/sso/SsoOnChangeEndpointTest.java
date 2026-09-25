package xyz.erupt.test.sso;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import xyz.erupt.core.util.SecretUtil;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The provider type dropdown fires the onchange endpoint with the whole form as the browser
 * serialises it: booleans, an empty role checkbox as a list of id objects, tags joined by
 * their separator. That body has to deserialise into the entity and come back with the preset.
 */
public class SsoOnChangeEndpointTest extends EruptApplicationTests {

    @Resource
    private TestRestTemplate rest;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptAppProp eruptAppProp;

    private HttpHeaders headers;

    @BeforeEach
    void login() {
        String pwd = eruptUpmsProp.getDefaultPassword();
        if (eruptAppProp.getPwdTransferEncrypt()) pwd = SecretUtil.encodeSecret(pwd, 3);
        Map<String, Object> body = rest.postForEntity("/erupt-api/login",
                Map.of("account", eruptUpmsProp.getDefaultAccount(), "pwd", pwd), Map.class).getBody();
        assertNotNull(body);
        headers = new HttpHeaders();
        headers.set("token", (String) body.get("token"));
        headers.set("erupt", "EruptSso");
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @SuppressWarnings("unchecked")
    void switchingTheTypeReturnsThePresetForABrowserShapedForm() {
        String form = """
                {"type":"FEISHU","status":true,"syncProfile":true,"autoCreate":true,"grantRolesOnLogin":false,
                 "scopes":"openid profile email","defaultRoles":[{"id":1}],"accountClaim":"preferred_username",
                 "nameClaim":"name","emailClaim":"email","icon":"fa fa-building"}
                """;
        ResponseEntity<Map> resp = rest.exchange("/erupt-api/data/onchange/EruptSso/type", HttpMethod.POST,
                new HttpEntity<>(form, headers), Map.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue((Boolean) resp.getBody().get("success"), String.valueOf(resp.getBody()));
        Map<String, Object> data = (Map<String, Object>) resp.getBody().get("data");
        Map<String, Object> formData = (Map<String, Object>) data.get("formData");
        assertEquals("https://open.feishu.cn/open-apis/authen/v1/user_info", formData.get("userInfoUrl"));
        assertEquals("fa-solid fa-feather-pointed", formData.get("icon"));
        Map<String, String> editExpr = (Map<String, String>) data.get("editExpr");
        assertEquals("edit.desc=\"App ID\"", editExpr.get("clientId"));
    }

}
