package xyz.erupt.test.upms;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import xyz.erupt.core.util.SecretUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.service.EruptMfaService;
import xyz.erupt.upms.util.TotpUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the enrolment endpoints exactly as the dialog does, then asks /mfa/status the way
 * the header does after the dialog closes. The header dot is driven by that answer.
 */
public class MfaEnrollHttpTest extends EruptApplicationTests {

    @Autowired
    private TestRestTemplate rest;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptAppProp eruptAppProp;

    @Resource
    private EruptMfaService eruptMfaService;

    @Resource
    private EruptDao dao;

    @Test
    @SuppressWarnings("unchecked")
    public void statusFlipsToBoundRightAfterEnrolment() {
        HttpHeaders auth = this.login();

        // status before: not bound, header shows the dot
        Map<String, Object> before = this.get("/erupt-api/mfa/status", auth);
        assertFalse((Boolean) before.get("bound"), "precondition: account starts unbound");

        // the dialog's step one
        Map<String, Object> enroll = this.post("/erupt-api/mfa/enroll", new HashMap<>(), auth);
        Map<String, Object> enrollData = (Map<String, Object>) enroll.get("data");
        String secret = (String) enrollData.get("secret");

        // the dialog's step two
        Map<String, String> confirmBody = new HashMap<>();
        confirmBody.put("code", TotpUtil.generate(secret, TotpUtil.currentCounter()));
        Map<String, Object> confirm = this.post("/erupt-api/mfa/enroll-confirm", confirmBody, auth);
        assertEquals("SUCCESS", confirm.get("status"), "enrolment must succeed: " + confirm.get("message"));

        // the database agrees
        EruptUser user = dao.lambdaQuery(EruptUser.class)
                .eq(EruptUser::getAccount, eruptUpmsProp.getDefaultAccount()).one();
        assertTrue(eruptMfaService.isBound(user), "the binding must be persisted");

        // and so must the endpoint the header re-reads when the dialog closes
        Map<String, Object> after = this.get("/erupt-api/mfa/status", auth);
        assertTrue((Boolean) after.get("bound"), "status must report bound, otherwise the dot stays");

        eruptMfaService.unbind(user.getId());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private HttpHeaders login() {
        String pwd = eruptUpmsProp.getDefaultPassword();
        if (eruptAppProp.getPwdTransferEncrypt()) pwd = SecretUtil.encodeSecret(pwd, 3);
        Map<String, String> body = new HashMap<>();
        body.put("account", eruptUpmsProp.getDefaultAccount());
        body.put("pwd", pwd);
        ResponseEntity<Map> resp = rest.postForEntity("/erupt-api/login", body, Map.class);
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", (String) resp.getBody().get("token"));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> get(String path, HttpHeaders auth) {
        return rest.exchange(path, HttpMethod.GET, new HttpEntity<>(auth), Map.class).getBody();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String path, Object body, HttpHeaders auth) {
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body, auth), Map.class).getBody();
    }

}
