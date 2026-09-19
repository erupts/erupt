package xyz.erupt.test.upms;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import xyz.erupt.core.util.SecretUtil;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.service.EruptTokenService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Changing the password ends every other session of the account: whoever was holding a
 * token minted with the old password is signed out, while the session that made the
 * change keeps working.
 */
public class ChangePwdSessionTest extends EruptApplicationTests {

    @Autowired
    private TestRestTemplate rest;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptAppProp eruptAppProp;

    @Resource
    private EruptTokenService eruptTokenService;

    @Test
    public void changingThePasswordSignsOutEveryOtherSession() {
        String oldPwd = eruptUpmsProp.getDefaultPassword();
        String newPwd = oldPwd + "-rotated";
        String mine = this.login(oldPwd);
        String other = this.login(oldPwd);
        String stranger = this.login(oldPwd);
        assertTrue(eruptTokenService.tokenExist(other) && eruptTokenService.tokenExist(stranger));

        try {
            assertEquals(HttpStatus.OK, this.changePwd(mine, oldPwd, newPwd).getStatusCode());
            assertTrue(eruptTokenService.tokenExist(mine), "the session that changed the password stays");
            assertFalse(eruptTokenService.tokenExist(other), "a session opened with the old password is gone");
            assertFalse(eruptTokenService.tokenExist(stranger), "every other session is gone, not just one");
            // and the survivor is still a working session, not just a leftover key
            assertEquals(HttpStatus.OK, rest.exchange("/erupt-api/userinfo", org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(this.tokenHeaders(mine)), Map.class).getStatusCode());
        } finally {
            // put the shared account back the way the other tests expect it
            this.changePwd(mine, newPwd, oldPwd);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ResponseEntity<Map> changePwd(String token, String pwd, String newPwd) {
        Map<String, String> body = new HashMap<>();
        body.put("pwd", this.transfer(pwd));
        body.put("newPwd", this.transfer(newPwd));
        body.put("newPwd2", this.transfer(newPwd));
        HttpHeaders headers = this.tokenHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.postForEntity("/erupt-api/change-pwd", new HttpEntity<>(body, headers), Map.class);
    }

    private HttpHeaders tokenHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(EruptReqHeaderConst.ERUPT_HEADER_TOKEN, token);
        return headers;
    }

    private String transfer(String pwd) {
        return eruptAppProp.getPwdTransferEncrypt() ? SecretUtil.encodeSecret(pwd, 3) : pwd;
    }

    @SuppressWarnings("unchecked")
    private String login(String pwd) {
        Map<String, String> body = new HashMap<>();
        body.put("account", eruptUpmsProp.getDefaultAccount());
        body.put("pwd", this.transfer(pwd));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> resp = rest.postForEntity("/erupt-api/login", new HttpEntity<>(body, headers), Map.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue((Boolean) resp.getBody().get("pass"), "login must pass: " + resp.getBody().get("reason"));
        return (String) resp.getBody().get("token");
    }

}
