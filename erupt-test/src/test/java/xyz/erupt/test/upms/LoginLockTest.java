package xyz.erupt.test.upms;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.service.EruptSessionService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repeated wrong passwords lock the account + IP pair for a while, and during the lock
 * even the right password is refused. The captcha kicks in after fewer failures than the
 * lock, so every attempt here solves it first, the way a bot that reads captchas would.
 */
public class LoginLockTest extends EruptApplicationTests {

    @Autowired
    private TestRestTemplate rest;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptAppProp eruptAppProp;

    @Resource
    private EruptSessionService sessionService;

    // The lock outlives a test method and the account is shared with other login tests,
    // so leave no counter or lock behind whichever IP form the servlet reported
    @BeforeEach
    @AfterEach
    void clearLockState() {
        sessionService.keys(SessionKey.LOGIN_LOCK).forEach(sessionService::remove);
        sessionService.keys(SessionKey.LOGIN_ERROR).forEach(sessionService::remove);
    }

    @Test
    public void tooManyWrongPasswordsLockEvenTheRightOne() {
        int max = eruptUpmsProp.getLoginLock().getMaxFailures();
        String minutes = String.valueOf(eruptUpmsProp.getLoginLock().getLockMinutes());
        for (int i = 0; i < max; i++) {
            Map<String, Object> fail = this.login("definitely-wrong-" + i);
            assertFalse((Boolean) fail.get("pass"));
            assertNull(fail.get("token"));
            // the last failure already reports the lock, the ones before it just a wrong password
            assertEquals(i == max - 1, ((String) fail.get("reason")).contains(minutes), "attempt " + (i + 1) + ": " + fail.get("reason"));
        }
        assertEquals(1, sessionService.keys(SessionKey.LOGIN_LOCK).size(), "one lock for the account + IP pair");
        assertTrue(sessionService.keys(SessionKey.LOGIN_ERROR).isEmpty(), "the counter is dropped with the lock");

        Map<String, Object> locked = this.login(eruptUpmsProp.getDefaultPassword());
        assertFalse((Boolean) locked.get("pass"), "the right password must not open a locked pair");
        assertNull(locked.get("token"));
        assertTrue(((String) locked.get("reason")).contains(String.valueOf(eruptUpmsProp.getLoginLock().getLockMinutes())),
                "the reason tells how long the lock lasts: " + locked.get("reason"));
    }

    @Test
    public void aWrongCaptchaCountsAsAFailureToo() {
        int max = eruptUpmsProp.getLoginLock().getMaxFailures();
        int captchaAfter = eruptAppProp.getVerifyCodeCount();
        // until the captcha is demanded a wrong code is ignored, so get there with wrong passwords
        for (int i = 0; i < captchaAfter; i++) this.login("wrong");
        // from here on the captcha is refused before the password is even looked at
        for (int i = captchaAfter; i < max; i++) {
            Map<String, Object> fail = this.login(eruptUpmsProp.getDefaultPassword(), "0000");
            assertFalse((Boolean) fail.get("pass"));
            assertTrue((Boolean) fail.get("useVerifyCode"));
        }
        assertEquals(1, sessionService.keys(SessionKey.LOGIN_LOCK).size(), "captcha failures must reach the lock");

        // once locked the lock answers first, even to a request that would fail the captcha anyway
        Map<String, Object> locked = this.login(eruptUpmsProp.getDefaultPassword(), "0000");
        assertTrue(((String) locked.get("reason")).contains(String.valueOf(eruptUpmsProp.getLoginLock().getLockMinutes())),
                "the lock, not the captcha, is the reason: " + locked.get("reason"));
    }

    @Test
    public void lockLiftsWhenItsKeyExpires() {
        int max = eruptUpmsProp.getLoginLock().getMaxFailures();
        for (int i = 0; i < max; i++) this.login("wrong");
        assertFalse((Boolean) this.login(eruptUpmsProp.getDefaultPassword()).get("pass"));

        // the lock is nothing but a key with a TTL, so its expiry is the whole unlock
        sessionService.keys(SessionKey.LOGIN_LOCK).forEach(sessionService::remove);
        Map<String, Object> ok = this.login(eruptUpmsProp.getDefaultPassword());
        assertTrue((Boolean) ok.get("pass"));
        assertNotNull(ok.get("token"));
    }

    @Test
    public void aSuccessResetsTheFailureCounter() {
        int max = eruptUpmsProp.getLoginLock().getMaxFailures();
        for (int i = 0; i < max - 1; i++) this.login("wrong");
        assertTrue((Boolean) this.login(eruptUpmsProp.getDefaultPassword()).get("pass"));
        assertTrue(sessionService.keys(SessionKey.LOGIN_ERROR).isEmpty(), "a good login clears the count");
        // the same number of failures again must not lock, the earlier ones no longer count
        for (int i = 0; i < max - 1; i++) this.login("wrong");
        assertTrue(sessionService.keys(SessionKey.LOGIN_LOCK).isEmpty());
        assertTrue((Boolean) this.login(eruptUpmsProp.getDefaultPassword()).get("pass"));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    // Log in with the captcha solved, the way a bot that reads captchas would
    private Map<String, Object> login(String pwd) {
        return this.login(pwd, null);
    }

    // Log in with a captcha fetched for this attempt; a non-null answer replaces the real text
    @SuppressWarnings("unchecked")
    private Map<String, Object> login(String pwd, String captchaAnswer) {
        if (eruptAppProp.getPwdTransferEncrypt()) pwd = SecretUtil.encodeSecret(pwd, 3);
        long mark = System.nanoTime();
        rest.getForEntity("/erupt-api/code-img?mark=" + mark, byte[].class);
        Object code = sessionService.get(SessionKey.VERIFY_CODE + mark);
        assertNotNull(code, "the captcha endpoint stores the text under its mark");
        Map<String, String> body = new HashMap<>();
        body.put("account", eruptUpmsProp.getDefaultAccount());
        body.put("pwd", pwd);
        body.put("verifyCode", null == captchaAnswer ? code.toString() : captchaAnswer);
        body.put("verifyCodeMark", String.valueOf(mark));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> resp = rest.postForEntity("/erupt-api/login", new HttpEntity<>(body, headers), Map.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        return resp.getBody();
    }

}
