package xyz.erupt.test.sso;

import com.sun.net.httpserver.HttpServer;
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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.model.EruptSsoBind;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End to end checks for delegated login against a stand-in identity provider: the browser
 * never carries a session token, one state buys exactly one session, and an identity the
 * system does not know is refused rather than quietly granted.
 */
public class SsoLoginTest extends EruptApplicationTests {

    private static final String PROVIDER = "unit-test";

    @Autowired
    private TestRestTemplate rest;

    @LocalServerPort
    private int port;

    // The redirects are the thing under test, so they must not be followed away
    private final RestTemplate noRedirect = new RestTemplate(new SimpleClientHttpRequestFactory() {
        @Override
        protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
            super.prepareConnection(connection, httpMethod);
            connection.setInstanceFollowRedirects(false);
        }
    });

    @Resource
    private EruptDao dao;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private TransactionTemplate transactionTemplate;

    private HttpServer idp;

    // what the stand-in provider answers on /userinfo, rewritten per test
    private volatile String userInfo;

    @BeforeEach
    void startIdp() throws Exception {
        this.userInfo = json("sub", "idp-subject-1", "preferred_username", eruptUpmsProp.getDefaultAccount());
        idp = HttpServer.create(new InetSocketAddress(0), 0);
        idp.createContext("/token", exchange -> respond(exchange, json("access_token", "access-token-1", "token_type", "Bearer")));
        idp.createContext("/userinfo", exchange -> respond(exchange, this.userInfo));
        idp.start();
        String base = "http://localhost:" + idp.getAddress().getPort();
        transactionTemplate.executeWithoutResult(status -> {
            EruptSso sso = new EruptSso();
            sso.setCode(PROVIDER);
            sso.setName("Unit Test IdP");
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
            sso.setEmailClaim("email");
            sso.setAutoCreate(false);
            dao.persist(sso);
        });
    }

    @AfterEach
    void stopIdp() {
        idp.stop(0);
        transactionTemplate.executeWithoutResult(status -> {
            EruptSso sso = dao.lambdaQuery(EruptSso.class).eq(EruptSso::getCode, PROVIDER).one();
            if (null == sso) return;
            dao.lambdaQuery(EruptSsoBind.class).eq(EruptSsoBind::getSso, sso).list()
                    .forEach(it -> dao.delete(dao.find(EruptSsoBind.class, it.getId())));
            dao.delete(dao.find(EruptSso.class, sso.getId()));
        });
    }

    @Test
    void enabledProviderIsOfferedOnTheLoginPage() {
        ResponseEntity<String> resp = rest.getForEntity("/erupt-api/sso/providers", String.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().contains(PROVIDER), "the provider has to reach the login page");
        assertFalse(resp.getBody().contains("s3cret"), "no client secret may leave the server");
    }

    @Test
    void authorizeRedirectCarriesStateAndPkce() {
        String location = this.authorize();
        assertTrue(location.contains("response_type=code"));
        assertTrue(location.contains("code_challenge_method=S256"));
        assertTrue(location.contains("code_challenge="));
        assertNotNull(param(location, "state"), "a redirect without state is a redirect without a reply address");
    }

    @Test
    void knownIdentityGetsASessionThroughTheTicket() {
        String state = param(this.authorize(), "state");
        String back = this.callback(state);
        String ticket = param(back, "ssoTicket");
        assertNotNull(ticket, "the callback has to hand back a ticket: " + back);
        assertFalse(back.contains("token="), "a session token must not travel in the redirect");

        Map<String, Object> login = this.exchange(ticket);
        assertTrue((Boolean) login.get("pass"), String.valueOf(login.get("reason")));
        assertNotNull(login.get("token"));
        assertEquals(eruptUpmsProp.getDefaultAccount(), login.get("account"));
    }

    @Test
    void ticketIsSpentOnFirstUse() {
        String ticket = param(this.callback(param(this.authorize(), "state")), "ssoTicket");
        assertTrue((Boolean) this.exchange(ticket).get("pass"));
        assertFalse((Boolean) this.exchange(ticket).get("pass"), "a ticket may only be cashed in once");
    }

    @Test
    void stateIsSpentOnFirstUse() {
        String state = param(this.authorize(), "state");
        assertNotNull(param(this.callback(state), "ssoTicket"));
        assertNotNull(param(this.callback(state), "ssoError"), "a replayed state must not mint a second session");
    }

    @Test
    void unknownIdentityIsRefusedWhenAutoCreateIsOff() {
        this.userInfo = json("sub", "idp-subject-2", "preferred_username", "nobody-here");
        String back = this.callback(param(this.authorize(), "state"));
        assertNotNull(param(back, "ssoError"), "an unknown identity must not be let in");
        assertNull(param(back, "ssoTicket"));
        assertNull(dao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, "nobody-here").one());
    }

    // ------------------------------------------------------------------ steps

    private String authorize() {
        ResponseEntity<Void> resp = noRedirect.getForEntity(this.url("/erupt-api/sso/authorize/" + PROVIDER), Void.class);
        assertEquals(HttpStatus.FOUND, resp.getStatusCode());
        return resp.getHeaders().getLocation().toString();
    }

    private String callback(String state) {
        ResponseEntity<Void> resp = noRedirect.getForEntity(
                this.url("/erupt-api/sso/callback/" + PROVIDER + "?state=" + state + "&code=authorization-code"), Void.class);
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
        assertNotNull(resp.getBody());
        return resp.getBody();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // ------------------------------------------------------------------ utils

    private static String param(String url, String name) {
        String query = URI.create(url).getQuery();
        if (null == query) {
            int mark = url.indexOf('?'); // a hash route keeps its query out of URI.getQuery()
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

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, String body) throws java.io.IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

}
