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
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.service.EruptMfaService;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.util.TotpUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End to end checks for the two step login: a bound account never receives a session
 * token from /login alone, and the code exchanged at /login-mfa is what mints it.
 */
public class MfaLoginTest extends EruptApplicationTests {

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

    @Resource
    private EruptSessionService sessionService;

    private String secret;

    /**
     * Spent counters live in the session store and outlive a test method, so a later test
     * would inherit the previous one's replay block. Clear the window the tests can reach.
     */
    @BeforeEach
    void clearSpentCounters() {
        EruptUser user = this.findUser();
        long counter = TotpUtil.currentCounter();
        for (long i = counter - 2; i <= counter + 2; i++) {
            sessionService.remove(SessionKey.MFA_USED + user.getId() + ":" + i);
        }
    }

    @AfterEach
    void tearDown() {
        EruptUser user = this.findUser();
        if (null != user) {
            eruptMfaService.unbind(user.getId());
        }
    }

    @Test
    public void loginWithoutMfaIssuesTokenDirectly() {
        Map<String, Object> body = this.login();
        assertTrue((Boolean) body.get("pass"));
        assertNotNull(body.get("token"));
        assertFalse((Boolean) body.getOrDefault("mfaRequired", false));
    }

    @Test
    public void boundAccountMustPassTheSecondStep() {
        this.bindAuthenticator();

        // step one: the password alone buys a ticket, never a session
        Map<String, Object> first = this.login();
        assertFalse((Boolean) first.get("pass"), "a bound account must not pass on the password alone");
        assertTrue((Boolean) first.get("mfaRequired"));
        assertNull(first.get("token"), "no token may exist before the second factor");
        String ticket = (String) first.get("mfaTicket");
        assertNotNull(ticket);

        // a wrong code keeps the user on the code screen, still without a token
        Map<String, Object> wrong = this.loginMfa(ticket, "000000");
        assertFalse((Boolean) wrong.get("pass"));
        assertNull(wrong.get("token"));
        assertTrue((Boolean) wrong.get("mfaRequired"));

        // step two: the real code mints the session
        Map<String, Object> second = this.loginMfa(ticket, TotpUtil.generate(secret, TotpUtil.currentCounter()));
        assertTrue((Boolean) second.get("pass"));
        assertNotNull(second.get("token"));

        // the ticket is single use, a replay of it buys nothing
        Map<String, Object> replay = this.loginMfa(ticket, TotpUtil.generate(secret, TotpUtil.currentCounter()));
        assertFalse((Boolean) replay.get("pass"));
        assertNull(replay.get("token"));
    }

    @Test
    public void aCodeCannotBeUsedTwice() {
        this.bindAuthenticator();
        String code = TotpUtil.generate(secret, TotpUtil.currentCounter());

        String firstTicket = (String) this.login().get("mfaTicket");
        assertTrue((Boolean) this.loginMfa(firstTicket, code).get("pass"));

        // same code, fresh ticket: the counter is already spent
        String secondTicket = (String) this.login().get("mfaTicket");
        Map<String, Object> replay = this.loginMfa(secondTicket, code);
        assertFalse((Boolean) replay.get("pass"), "a one-time code must not work twice");
        assertNull(replay.get("token"));
    }

    @Test
    public void recoveryCodeWorksOnceAndThenBurns() {
        List<String> codes = this.bindAuthenticator();
        String recovery = codes.get(0);

        String ticket = (String) this.login().get("mfaTicket");
        assertTrue((Boolean) this.loginMfa(ticket, recovery).get("pass"));
        assertEquals(codes.size() - 1, eruptMfaService.countRecoveryCodes(this.findUser().getId()));

        String nextTicket = (String) this.login().get("mfaTicket");
        assertFalse((Boolean) this.loginMfa(nextTicket, recovery).get("pass"), "a recovery code is single use");
    }

    /**
     * The recovery codes are a JSON array in one column of e_upms_user, not a side table.
     * Guarding the shape here because ddl-auto=update can never widen that column later,
     * so a silent switch back to a collection table, or an overflow, must fail loudly.
     */
    @Test
    public void recoveryCodesLiveInOneJsonColumn() {
        List<String> codes = this.bindAuthenticator();

        Number sideTables = (Number) dao.getEntityManager().createNativeQuery(
                "select count(*) from information_schema.tables where upper(table_name) like 'E_UPMS_USER_MFA%'"
        ).getSingleResult();
        assertEquals(0L, sideTables.longValue(), "recovery codes must not create a side table");

        Object[] column = (Object[]) dao.getEntityManager().createNativeQuery(
                "select character_maximum_length, data_type from information_schema.columns" +
                        " where upper(table_name) = 'E_UPMS_USER' and upper(column_name) = 'MFA_RECOVERY_CODES'"
        ).getSingleResult();
        long width = ((Number) column[0]).longValue();

        String stored = (String) dao.getEntityManager().createNativeQuery(
                "select mfa_recovery_codes from e_upms_user where id = " + this.findUser().getId()
        ).getSingleResult();
        assertTrue(stored.startsWith("[") && stored.endsWith("]"), "stored as a JSON array: " + stored);
        assertTrue(stored.length() <= width, "the JSON must fit the column: " + stored.length() + " > " + width);
        // room to raise the code count without a schema change no database can apply
        assertTrue(width >= stored.length() * 2L, "leave headroom: width " + width + " for " + stored.length());

        // and it still round trips into the entity
        assertEquals(codes.size(), this.findUser().getMfaRecoveryCodes().size());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Enrol an authenticator the way the UI does, through the service rather than the database,
     * so the test exercises the same state the controller writes.
     */
    private List<String> bindAuthenticator() {
        EruptUser user = this.findUser();
        // the enrolment screen shows the secret in groups of four, which base32Decode tolerates
        secret = eruptMfaService.startEnroll(user).getSecret();
        List<String> codes = eruptMfaService.confirmEnroll(user, TotpUtil.generate(secret, TotpUtil.currentCounter()));
        assertTrue(eruptMfaService.isBound(this.findUser()));
        return codes;
    }

    private EruptUser findUser() {
        return dao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, eruptUpmsProp.getDefaultAccount()).one();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> login() {
        String pwd = eruptUpmsProp.getDefaultPassword();
        if (eruptAppProp.getPwdTransferEncrypt()) pwd = SecretUtil.encodeSecret(pwd, 3);
        Map<String, String> body = new HashMap<>();
        body.put("account", eruptUpmsProp.getDefaultAccount());
        body.put("pwd", pwd);
        return this.post("/erupt-api/login", body);
    }

    private Map<String, Object> loginMfa(String ticket, String code) {
        Map<String, String> body = new HashMap<>();
        body.put("mfaTicket", ticket);
        body.put("code", code);
        return this.post("/erupt-api/login-mfa", body);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String path, Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> resp = rest.postForEntity(path, new HttpEntity<>(body, headers), Map.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        return resp.getBody();
    }

}
