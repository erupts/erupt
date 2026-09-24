package xyz.erupt.test.upms;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import xyz.erupt.core.util.SecretUtil;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A signed-in user maintains their own avatar and display name through the profile endpoint;
 * the change is visible on the next userinfo call without a fresh login, and anything that is
 * not an image or not an address is turned away.
 */
public class ProfileTest extends EruptApplicationTests {

    @Autowired
    private TestRestTemplate rest;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptAppProp eruptAppProp;

    @Test
    @SuppressWarnings("unchecked")
    public void profileRoundTrip() {
        String token = this.login();
        EruptUser before = eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, eruptUpmsProp.getDefaultAccount()).one();
        try {
            String avatar = this.uploadAvatar(token, "me.png", MediaType.IMAGE_PNG_VALUE);
            assertTrue(avatar.startsWith("/avatar/"), "avatars land under their own folder: " + avatar);

            Map<String, Object> saved = this.updateProfile(token, "Renamed", avatar);
            assertEquals("SUCCESS", saved.get("status"), String.valueOf(saved.get("message")));

            Map<String, Object> userinfo = this.userinfo(token);
            assertEquals("Renamed", userinfo.get("nickname"));
            assertEquals(avatar, userinfo.get("avatar"));
            assertEquals(eruptUpmsProp.getDefaultAccount(), userinfo.get("account"));

            // an empty avatar clears it, a blank name is refused and a scheme that is not http is refused
            assertEquals("SUCCESS", this.updateProfile(token, "Renamed", null).get("status"));
            assertNull(this.userinfo(token).get("avatar"));
            assertNotEquals("SUCCESS", this.updateProfile(token, "  ", null).get("status"));
            assertNotEquals("SUCCESS", this.updateProfile(token, "Renamed", "javascript:alert(1)").get("status"));
            assertNotEquals(HttpStatus.OK, this.uploadAvatarRaw(token, "evil.html", MediaType.TEXT_HTML_VALUE).getStatusCode());
        } finally {
            this.updateProfile(token, before.getName(), before.getAvatar());
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String uploadAvatar(String token, String filename, String contentType) {
        ResponseEntity<Map> resp = this.uploadAvatarRaw(token, filename, contentType);
        assertEquals(HttpStatus.OK, resp.getStatusCode(), String.valueOf(resp.getBody()));
        return (String) resp.getBody().get("data");
    }

    private ResponseEntity<Map> uploadAvatarRaw(String token, String filename, String contentType) {
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(contentType));
        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(new ByteArrayResource(new byte[]{1, 2, 3}) {
            @Override
            public String getFilename() {
                return filename;
            }
        }, fileHeaders);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filePart);
        HttpHeaders headers = this.tokenHeaders(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return rest.postForEntity("/erupt-api/profile/avatar", new HttpEntity<>(body, headers), Map.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> updateProfile(String token, String name, String avatar) {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("avatar", avatar);
        HttpHeaders headers = this.tokenHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> resp = rest.postForEntity("/erupt-api/profile", new HttpEntity<>(body, headers), Map.class);
        return null == resp.getBody() ? Map.of("status", String.valueOf(resp.getStatusCode())) : resp.getBody();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> userinfo(String token) {
        return rest.exchange("/erupt-api/userinfo", HttpMethod.GET, new HttpEntity<>(this.tokenHeaders(token)), Map.class).getBody();
    }

    private HttpHeaders tokenHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(EruptReqHeaderConst.ERUPT_HEADER_TOKEN, token);
        return headers;
    }

    @SuppressWarnings("unchecked")
    private String login() {
        String pwd = eruptUpmsProp.getDefaultPassword();
        Map<String, String> body = new HashMap<>();
        body.put("account", eruptUpmsProp.getDefaultAccount());
        body.put("pwd", eruptAppProp.getPwdTransferEncrypt() ? SecretUtil.encodeSecret(pwd, 3) : pwd);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> resp = rest.postForEntity("/erupt-api/login", new HttpEntity<>(body, headers), Map.class);
        assertTrue((Boolean) resp.getBody().get("pass"), "login must pass: " + resp.getBody().get("reason"));
        return (String) resp.getBody().get("token");
    }

}
