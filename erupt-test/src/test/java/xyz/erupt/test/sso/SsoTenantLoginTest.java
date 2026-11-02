package xyz.erupt.test.sso;

import com.sun.net.httpserver.HttpServer;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import xyz.erupt.sso.constant.SsoProviderType;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.model.EruptSsoBind;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.helper.UpmsSecurityHelper;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.tenant.UpmsTenantSessions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A tenant signs in through providers of its own: the provider row, the binding and the user all
 * live in the tenant, and the session is the one the tenant module hands out. An OAuth2 provider of
 * a tenant calls back on the platform's address, so a tenant reusing the platform's app needs no
 * further address registered there; the tenant comes from the state. The platform's providers and
 * users stay out of reach, and a provider code may be used by the platform and a tenant alike.
 */
public class SsoTenantLoginTest extends EruptApplicationTests {

    private static final String TENANT_CODE = StandInTenantModule.TENANT_CODE;

    private static final String TENANT_ID = StandInTenantModule.TENANT_ID;

    // the same code on a platform row and on a tenant row
    private static final String PROVIDER = "tenant-test";

    private static final String ACCOUNT = "sso-tenant-user";

    private static final String PLATFORM_CALLBACK = "/erupt-api/sso/callback/" + PROVIDER;

    private static final String TENANT_CALLBACK = "/erupt-api/sso/tenant/" + TENANT_CODE + "/callback/" + PROVIDER;

    @Autowired
    private TestRestTemplate rest;

    @LocalServerPort
    private int port;

    private final RestTemplate noRedirect = new RestTemplate(new SimpleClientHttpRequestFactory() {
        @Override
        protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
            super.prepareConnection(connection, httpMethod);
            connection.setInstanceFollowRedirects(false);
        }
    });

    @Resource
    private UpmsTenantSessions sessions;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    private HttpServer idp;

    private volatile String userInfo;

    @BeforeEach
    void setUp() throws Exception {
        StandInTenantModule.LOGINS.clear();
        StandInTenantModule.refusal = null;
        this.userInfo = json("sub", "tenant-subject-1", "preferred_username", ACCOUNT);
        idp = HttpServer.create(new InetSocketAddress(0), 0);
        idp.createContext("/token", exchange -> respond(exchange, json("access_token", "access-token-1", "token_type", "Bearer")));
        idp.createContext("/userinfo", exchange -> respond(exchange, this.userInfo));
        idp.start();
        String base = "http://localhost:" + idp.getAddress().getPort();
        transactionTemplate.executeWithoutResult(status -> eruptDao.persist(provider(base, "Platform IdP")));
        sessions.runAs(TENANT_ID, em -> {
            em.persist(provider(base, "Acme IdP"));
            EruptUser user = new EruptUser();
            user.setAccount(ACCOUNT);
            user.setName("Acme User");
            user.setStatus(true);
            user.setIsAdmin(false);
            user.setEncrypt(true);
            UpmsSecurityHelper.applyPassword(user, "secret", true);
            em.persist(user);
        });
    }

    @AfterEach
    void tearDown() {
        idp.stop(0);
        sessions.runAs(TENANT_ID, em -> {
            eruptDao.lambdaQuery(em, EruptSsoBind.class).list().forEach(em::remove);
            eruptDao.lambdaQuery(em, EruptSso.class).list().forEach(em::remove);
            eruptDao.lambdaQuery(em, EruptUser.class).list().forEach(em::remove);
        });
        transactionTemplate.executeWithoutResult(status -> {
            EruptSso sso = eruptDao.lambdaQuery(EruptSso.class).eq(EruptSso::getCode, PROVIDER).one();
            if (null != sso) eruptDao.delete(sso);
        });
    }

    @Test
    void theTenantLoginPageOffersOnlyTheTenantsProviders() {
        String tenant = rest.getForObject("/erupt-api/sso/providers?tenant=" + TENANT_CODE, String.class);
        assertTrue(tenant.contains("Acme IdP"), tenant);
        assertFalse(tenant.contains("Platform IdP"), tenant);

        String platform = rest.getForObject("/erupt-api/sso/providers", String.class);
        assertTrue(platform.contains("Platform IdP"), platform);
        assertFalse(platform.contains("Acme IdP"), platform);

        // an unknown code reads like a tenant without providers
        assertEquals("[]", rest.getForObject("/erupt-api/sso/providers?tenant=nobody", String.class));
    }

    @Test
    void theTenantSignsInOnThePlatformCallbackAndGetsTheTenantSession() {
        String authorize = this.authorize(TENANT_CODE);
        String redirectUri = URLDecoder.decode(param(authorize, "redirect_uri"), StandardCharsets.UTF_8);
        assertTrue(redirectUri.endsWith("/erupt-api/sso/callback/" + PROVIDER), "the platform's address: " + redirectUri);

        String back = this.callback(PLATFORM_CALLBACK, param(authorize, "state"));
        assertTrue(back.contains("/#/passport/tenant?tenantCode=" + TENANT_CODE + "&"), "back to the tenant login page: " + back);
        String ticket = param(back, "ssoTicket");
        assertNotNull(ticket, back);

        Map<String, Object> login = this.exchange(ticket);
        assertTrue((Boolean) login.get("pass"), String.valueOf(login.get("reason")));
        assertTrue(String.valueOf(login.get("token")).startsWith("t_stand_in_"), "the session is the tenant module's");
        assertEquals(1, StandInTenantModule.LOGINS.size());
        EruptUser user = StandInTenantModule.LOGINS.get(0);
        assertEquals(ACCOUNT, user.getAccount());
        assertEquals(TENANT_ID, user.getTenantId());

        // the binding is the tenant's, the platform does not see it
        assertEquals(1L, (long) sessions.callAs(TENANT_ID, em -> eruptDao.lambdaQuery(em, EruptSsoBind.class)
                .eq(EruptSsoBind::getSubject, "tenant-subject-1").count()));
        assertEquals(0L, (long) eruptDao.lambdaQuery(EruptSsoBind.class).eq(EruptSsoBind::getSubject, "tenant-subject-1").count());
    }

    @Test
    void theTenantsOwnPathIsAcceptedForTheTenantOnly() {
        // a tenant row with its own redirect URI registered calls back on the tenant path
        String back = this.callback(TENANT_CALLBACK, param(this.authorize(TENANT_CODE), "state"));
        assertNotNull(param(back, "ssoTicket"), back);

        // the state names the tenant: another tenant's path cannot cash it in
        back = this.callback("/erupt-api/sso/tenant/other-tenant/callback/" + PROVIDER, param(this.authorize(TENANT_CODE), "state"));
        assertNotNull(param(back, "ssoError"), back);
        assertNull(param(back, "ssoTicket"));
        assertEquals(1, StandInTenantModule.LOGINS.size());
    }

    @Test
    void aProviderRefusalOnThePlatformCallbackReturnsToTheTenantLoginPage() {
        String state = param(this.authorize(TENANT_CODE), "state");
        ResponseEntity<Void> resp = noRedirect.getForEntity(
                this.url(PLATFORM_CALLBACK + "?state=" + state + "&error=access_denied"), Void.class);
        String back = resp.getHeaders().getLocation().toString();
        assertTrue(back.contains("/#/passport/tenant?tenantCode=" + TENANT_CODE + "&"), back);
        assertEquals("access_denied", param(back, "ssoError"));
    }

    @Test
    void aPlatformAccountOfTheSameNameIsNotATenantMatch() {
        // the platform's admin account is unknown inside the tenant, and the row does not auto create
        this.userInfo = json("sub", "tenant-subject-2", "preferred_username", eruptUpmsProp.getDefaultAccount());
        String authorize = this.authorize(TENANT_CODE);
        String back = this.callback(PLATFORM_CALLBACK, param(authorize, "state"));
        assertNotNull(param(back, "ssoError"), back);
        assertTrue(StandInTenantModule.LOGINS.isEmpty());
    }

    @Test
    void anAutoCreatedUserBelongsToTheTenant() {
        sessions.runAs(TENANT_ID, em -> eruptDao.lambdaQuery(em, EruptSso.class).list().forEach(it -> it.setAutoCreate(true)));
        this.userInfo = json("sub", "tenant-subject-3", "preferred_username", "sso-tenant-new");
        String authorize = this.authorize(TENANT_CODE);
        String back = this.callback(PLATFORM_CALLBACK, param(authorize, "state"));
        assertNotNull(param(back, "ssoTicket"), back);

        EruptUser created = sessions.callAs(TENANT_ID, em -> eruptDao.lambdaQuery(em, EruptUser.class)
                .eq(EruptUser::getAccount, "sso-tenant-new").one());
        assertNotNull(created);
        assertEquals(TENANT_ID, created.getTenantId());
        assertNull(eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, "sso-tenant-new").one());
    }

    @Test
    void aTenantTheModuleRefusesGetsTheReasonInsteadOfATicket() {
        StandInTenantModule.refusal = "company expired";
        String authorize = this.authorize(TENANT_CODE);
        String back = this.callback(PLATFORM_CALLBACK, param(authorize, "state"));
        assertNull(param(back, "ssoTicket"));
        // a failure on the platform's address still ends on the tenant's login page
        assertTrue(back.contains("/#/passport/tenant?tenantCode=" + TENANT_CODE + "&"), back);
        assertEquals("company expired", URLDecoder.decode(param(back, "ssoError"), StandardCharsets.UTF_8));
    }

    @Test
    void aTenantsSamlProviderKeepsTheTenantsOwnConsumerAddress() {
        // an IdP tells service providers apart by the consumer URL, so a tenant's SAML row cannot
        // share the platform's
        sessions.runAs(TENANT_ID, em -> {
            EruptSso saml = provider("https://idp.test", "Acme SAML");
            saml.setCode("tenant-saml");
            saml.setType(SsoProviderType.SAML);
            saml.setAuthorizeUrl("https://idp.test/sso");
            em.persist(saml);
        });
        String metadata = rest.getForObject("/erupt-api/sso/tenant/" + TENANT_CODE + "/saml/tenant-saml/metadata", String.class);
        assertTrue(metadata.contains("/erupt-api/sso/tenant/" + TENANT_CODE + "/callback/tenant-saml\""), metadata);
    }

    @Test
    void anUnknownTenantCannotStartASignOn() {
        String back = this.authorize("nobody");
        assertTrue(back.contains("/#/passport/tenant?tenantCode=nobody&"), back);
        assertNotNull(param(back, "ssoError"));
    }

    // ------------------------------------------------------------------ steps

    private static EruptSso provider(String base, String name) {
        EruptSso sso = new EruptSso();
        sso.setCode(PROVIDER);
        sso.setName(name);
        sso.setStatus(true);
        sso.setSort(0);
        sso.setAuthorizeUrl(base + "/authorize");
        sso.setTokenUrl(base + "/token");
        sso.setUserInfoUrl(base + "/userinfo");
        sso.setClientId("erupt");
        sso.setClientSecret("s3cret");
        sso.setScopes("openid profile");
        sso.setAccountClaim("preferred_username");
        sso.setNameClaim("name");
        sso.setAutoCreate(false);
        return sso;
    }

    private String authorize(String tenantCode) {
        ResponseEntity<Void> resp = noRedirect.getForEntity(
                this.url("/erupt-api/sso/authorize/" + PROVIDER + "?tenant=" + tenantCode), Void.class);
        assertEquals(HttpStatus.FOUND, resp.getStatusCode());
        return resp.getHeaders().getLocation().toString();
    }

    private String callback(String path, String state) {
        ResponseEntity<Void> resp = noRedirect.getForEntity(this.url(path + "?state=" + state + "&code=authorization-code"), Void.class);
        assertEquals(HttpStatus.FOUND, resp.getStatusCode());
        return resp.getHeaders().getLocation().toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> exchange(String ticket) {
        Map<String, String> body = new HashMap<>();
        body.put("ssoTicket", ticket);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> resp = rest.postForEntity("/erupt-api/sso/exchange", new HttpEntity<>(body, headers), Map.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        return resp.getBody();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private static String param(String url, String name) {
        String query = URI.create(url).getRawQuery();
        if (null == query) {
            int mark = url.indexOf('?');
            if (mark < 0) return null;
            query = url.substring(mark + 1);
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) return pair.substring(eq + 1);
        }
        return null;
    }

    private static String json(String... kv) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < kv.length; i += 2) {
            if (i > 0) sb.append(',');
            sb.append('"').append(kv[i]).append("\":\"").append(kv[i + 1]).append('"');
        }
        return sb.append('}').toString();
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Connection", "close");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

}
