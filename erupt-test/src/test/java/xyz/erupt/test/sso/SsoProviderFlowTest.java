package xyz.erupt.test.sso;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.sso.constant.SsoProviderType;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.model.EruptSsoBind;
import xyz.erupt.sso.model.data_proxy.EruptSsoDataProxy;
import xyz.erupt.sso.service.EruptSsoBindService;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The provider presets: what picking one puts into the form, and that the three providers
 * with an exchange of their own (DingTalk, WeCom, WeChat) are driven the way they expect and
 * leave the binding carrying the identifiers other modules ask for.
 */
public class SsoProviderFlowTest extends EruptApplicationTests {

    private static final List<String> CODES = List.of("ut-dingtalk", "ut-wecom", "ut-wechat");

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
    private EruptDao dao;

    @Resource
    private EruptSsoDataProxy ssoDataProxy;

    @Resource
    private EruptSsoBindService bindService;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private TransactionTemplate transactionTemplate;

    private HttpServer idp;

    private String base;

    // what the stand-in provider saw: request headers and bodies by path, call counts by path
    private final Map<String, String> seenHeader = new ConcurrentHashMap<>();

    private final Map<String, String> seenBody = new ConcurrentHashMap<>();

    private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

    @BeforeEach
    void startIdp() throws Exception {
        idp = HttpServer.create(new InetSocketAddress(0), 0);
        idp.start();
        base = "http://localhost:" + idp.getAddress().getPort();
    }

    @AfterEach
    void stopIdp() {
        idp.stop(0);
        transactionTemplate.executeWithoutResult(status -> {
            for (EruptSso sso : dao.lambdaQuery(EruptSso.class).in(EruptSso::getCode, CODES).list()) {
                dao.lambdaQuery(EruptSsoBind.class).eq(EruptSsoBind::getSso, sso).list()
                        .forEach(it -> dao.delete(dao.find(EruptSsoBind.class, it.getId())));
                dao.delete(dao.find(EruptSso.class, sso.getId()));
            }
        });
    }

    // ---------------------------------------------------------------- presets

    @Test
    void pickingAPresetFillsEndpointsAndClaimsButKeepsALabelAlreadyTyped() {
        EruptSso draft = new EruptSso();
        draft.setType(SsoProviderType.FEISHU);
        draft.setName("Company login");
        draft.setIcon("fa fa-building");
        Map<String, Object> form = ssoDataProxy.populateForm(draft, new String[0]);
        assertEquals("fa-solid fa-feather-pointed", form.get("icon"), "the icon follows the type even when one was set");
        assertEquals("https://open.feishu.cn/open-apis/authen/v2/oauth/token", form.get("tokenUrl"));
        assertEquals("open_id", form.get("openIdClaim"));
        assertEquals("mobile", form.get("phoneClaim"));
        assertEquals("feishu", form.get("code"), "a blank code is suggested from the type");
        assertFalse(form.containsKey("name"), "a name the admin already typed is left alone");
        assertTrue(form.containsKey("issuer") && null == form.get("issuer"), "a plain OAuth2 preset clears the issuer");

        draft.setType(SsoProviderType.CUSTOM);
        assertTrue(ssoDataProxy.populateForm(draft, new String[0]).isEmpty(), "custom fills nothing");
    }

    @Test
    void credentialHintsFollowTheProvider() {
        EruptSso draft = new EruptSso();
        draft.setType(SsoProviderType.WECOM);
        Map<String, String> expr = ssoDataProxy.buildEditExpr(draft, new String[0]);
        assertEquals("edit.desc=\"CorpID\"", expr.get("clientId"));
        draft.setType(SsoProviderType.GITHUB);
        assertEquals("edit.desc=\"\"", ssoDataProxy.buildEditExpr(draft, new String[0]).get("clientId"), "a preset without a hint clears the previous one");
    }

    @Test
    void aPlaceholderLeftInAnEndpointIsRefusedAtSave() {
        EruptSso draft = new EruptSso();
        draft.setType(SsoProviderType.KEYCLOAK);
        draft.setIssuer("https://<host>/realms/<realm>");
        EruptWebApiRuntimeException e = assertThrows(EruptWebApiRuntimeException.class, () -> ssoDataProxy.beforeAdd(draft));
        assertTrue(e.getMessage().contains("<host>"), e.getMessage());
    }

    @Test
    void everyPresetIsConsistent() {
        for (SsoProviderType type : SsoProviderType.values()) {
            SsoProviderType.Preset preset = type.preset();
            if (null == preset) continue;
            boolean spelledOut = null != preset.getAuthorizeUrl() && null != preset.getTokenUrl() && null != preset.getUserInfoUrl();
            assertTrue(spelledOut || null != preset.getIssuer(), type + " has to name an issuer or all three endpoints");
            assertTrue(type.flow() == SsoProviderType.Flow.OAUTH2 || spelledOut, type + " cannot rely on discovery");
            assertNotNull(preset.getScopes(), type + " has no scopes");
            assertNotNull(preset.getAccountClaim(), type + " has no account claim");
        }
    }

    // ------------------------------------------------------------------ flows

    @Test
    void dingTalkExchangesAJsonBodyAndSendsTheTokenInItsOwnHeader() {
        this.stub("/v1.0/oauth2/userAccessToken", "{\"accessToken\":\"ding-token\",\"expireIn\":7200}");
        this.stub("/v1.0/contact/users/me", json("nick", "Ding User", "openId", "ding-open-1", "unionId", "ding-union-1",
                "account", eruptUpmsProp.getDefaultAccount()));
        this.provider(SsoProviderType.DINGTALK, "ut-dingtalk", base + "/oauth2/auth", base + "/v1.0/oauth2/userAccessToken", base + "/v1.0/contact/users/me");

        String location = this.authorize("ut-dingtalk");
        assertTrue(location.startsWith(base + "/oauth2/auth?"), location);
        assertEquals("consent", param(location, "prompt"));
        assertNull(param(location, "code_challenge"), "no PKCE on a flow that does not know it");

        assertNotNull(param(this.callback("ut-dingtalk", param(location, "state")), "ssoTicket"));
        assertTrue(seenBody.get("/v1.0/oauth2/userAccessToken").contains("\"grantType\":\"authorization_code\""), seenBody.toString());
        assertEquals("ding-token", seenHeader.get("/v1.0/contact/users/me"));

        EruptSsoBind bind = this.bind("ut-dingtalk");
        assertEquals("ding-open-1", bind.getSubject());
        assertEquals("ding-union-1", bind.getOpenId(), "the messaging id for DingTalk is the unionId");
        assertNotNull(bind.getLastLoginTime());
        assertEquals("ding-union-1", bindService.openId(this.defaultUser().getId(), "ut-dingtalk").orElse(null));
        assertEquals("Ding User", bindService.claim(this.defaultUser().getId(), "ut-dingtalk", "nick").orElse(null));
    }

    @Test
    void weComResolvesTheCodeWithACachedCorpTokenAndMergesTheDetailCall() {
        this.stub("/cgi-bin/gettoken", "{\"errcode\":0,\"errmsg\":\"ok\",\"access_token\":\"corp-token\",\"expires_in\":7200}");
        this.stub("/cgi-bin/auth/getuserinfo", "{\"errcode\":0,\"userid\":\"zhangsan\",\"user_ticket\":\"ticket-1\"}");
        this.stub("/cgi-bin/user/get", json("errcode", "0", "userid", "zhangsan", "name", "Zhang San", "account", eruptUpmsProp.getDefaultAccount()));
        this.stub("/cgi-bin/auth/getuserdetail", "{\"errcode\":0,\"userid\":\"zhangsan\",\"mobile\":\"13900000000\"}");
        this.provider(SsoProviderType.WECOM, "ut-wecom", base + "/wwlogin/sso/login?login_type=CorpApp&agentid=1000002",
                base + "/cgi-bin/gettoken", base + "/cgi-bin/user/get");

        String location = this.authorize("ut-wecom");
        assertEquals("1000002", param(location, "agentid"), "the agent id configured in the URL survives");
        assertEquals("corp-id", param(location, "appid"));
        assertNotNull(param(this.callback("ut-wecom", param(location, "state")), "ssoTicket"));
        assertTrue(seenBody.get("/cgi-bin/auth/getuserdetail").contains("ticket-1"));

        EruptSsoBind bind = this.bind("ut-wecom");
        assertEquals("zhangsan", bind.getSubject());
        assertEquals("zhangsan", bind.getOpenId());
        assertEquals("13900000000", bindService.claim(this.defaultUser().getId(), "ut-wecom", "mobile").orElse(null),
                "the detail call's fields are merged into the snapshot");

        assertNotNull(param(this.callback("ut-wecom", param(this.authorize("ut-wecom"), "state")), "ssoTicket"));
        assertEquals(1, calls.get("/cgi-bin/gettoken").get(), "the corp token is fetched once, not per login");
        assertEquals(2, calls.get("/cgi-bin/auth/getuserinfo").get());
    }

    @Test
    void weComRefusesAVisitorWhoIsNotAMember() {
        this.stub("/cgi-bin/gettoken", "{\"errcode\":0,\"access_token\":\"corp-token\",\"expires_in\":7200}");
        this.stub("/cgi-bin/auth/getuserinfo", "{\"errcode\":0,\"openid\":\"outsider\"}");
        this.provider(SsoProviderType.WECOM, "ut-wecom", base + "/wwlogin", base + "/cgi-bin/gettoken", base + "/cgi-bin/user/get");
        String back = this.callback("ut-wecom", param(this.authorize("ut-wecom"), "state"));
        assertNotNull(param(back, "ssoError"), back);
        assertNull(param(back, "ssoTicket"));
    }

    @Test
    void tencentStyleErrorsAreReportedEvenOnHttp200() {
        this.stub("/cgi-bin/gettoken", "{\"errcode\":40013,\"errmsg\":\"invalid corpid\"}");
        this.provider(SsoProviderType.WECOM, "ut-wecom", base + "/wwlogin", base + "/cgi-bin/gettoken", base + "/cgi-bin/user/get");
        String back = this.callback("ut-wecom", param(this.authorize("ut-wecom"), "state"));
        assertTrue(param(back, "ssoError").contains("40013"), back);
    }

    @Test
    void weChatPassesTheOpenidFromTheTokenCallToTheUserInfoCall() {
        this.stub("/sns/oauth2/access_token", "{\"access_token\":\"wx-token\",\"openid\":\"wx-open-1\",\"unionid\":\"wx-union-1\"}");
        this.stub("/sns/userinfo", json("openid", "wx-open-1", "unionid", "wx-union-1", "nickname", "Nick", "account", eruptUpmsProp.getDefaultAccount()));
        this.provider(SsoProviderType.WECHAT, "ut-wechat", base + "/connect/qrconnect#wechat_redirect", base + "/sns/oauth2/access_token", base + "/sns/userinfo");

        String location = this.authorize("ut-wechat");
        assertTrue(location.endsWith("#wechat_redirect"), "the fragment stays last: " + location);
        assertEquals("snsapi_login", param(location, "scope"));
        assertNotNull(param(this.callback("ut-wechat", param(location, "state")), "ssoTicket"));
        assertTrue(seenBody.get("/sns/oauth2/access_token").contains("secret=corp-secret"));
        assertTrue(seenBody.get("/sns/userinfo").contains("openid=wx-open-1"), seenBody.get("/sns/userinfo"));

        EruptSsoBind bind = this.bind("ut-wechat");
        assertEquals("wx-open-1", bind.getOpenId());
    }

    // --------------------------------------------------------------- fixtures

    /**
     * A provider row built from its preset, with the endpoints pointed at the stand-in server.
     * The account is matched on a test-only claim so no real profile field is touched.
     */
    private void provider(SsoProviderType type, String code, String authorizeUrl, String tokenUrl, String userInfoUrl) {
        SsoProviderType.Preset preset = type.preset();
        transactionTemplate.executeWithoutResult(status -> {
            EruptSso sso = new EruptSso();
            sso.setType(type);
            sso.setCode(code);
            sso.setName(preset.getName());
            sso.setStatus(true);
            sso.setSort(0);
            sso.setAuthorizeUrl(authorizeUrl);
            sso.setTokenUrl(tokenUrl);
            sso.setUserInfoUrl(userInfoUrl);
            sso.setClientId("corp-id");
            sso.setClientSecret("corp-secret");
            sso.setScopes(preset.getScopes());
            sso.setAccountClaim("account");
            sso.setNameClaim(preset.getNameClaim());
            sso.setEmailClaim(preset.getEmailClaim());
            sso.setOpenIdClaim(preset.getOpenIdClaim());
            sso.setAutoCreate(false);
            sso.setSyncProfile(false);
            dao.persist(sso);
        });
    }

    private void stub(String path, String body) {
        calls.put(path, new AtomicInteger());
        idp.createContext(path, exchange -> {
            calls.get(path).incrementAndGet();
            String header = exchange.getRequestHeaders().getFirst("x-acs-dingtalk-access-token");
            if (null != header) seenHeader.put(path, header);
            String query = exchange.getRequestURI().getRawQuery();
            String posted = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            seenBody.put(path, (null == query ? "" : query) + posted);
            respond(exchange, body);
        });
    }

    private EruptSsoBind bind(String code) {
        return transactionTemplate.execute(status -> {
            EruptSso sso = dao.lambdaQuery(EruptSso.class).eq(EruptSso::getCode, code).one();
            return dao.lambdaQuery(EruptSsoBind.class).eq(EruptSsoBind::getSso, sso).one();
        });
    }

    private EruptUser defaultUser() {
        return dao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, eruptUpmsProp.getDefaultAccount()).one();
    }

    // ------------------------------------------------------------------ steps

    private String authorize(String code) {
        ResponseEntity<Void> resp = noRedirect.getForEntity(this.url("/erupt-api/sso/authorize/" + code), Void.class);
        assertEquals(HttpStatus.FOUND, resp.getStatusCode());
        return resp.getHeaders().getLocation().toString();
    }

    private String callback(String code, String state) {
        ResponseEntity<Void> resp = noRedirect.getForEntity(
                this.url("/erupt-api/sso/callback/" + code + "?state=" + state + "&code=authorization-code"), Void.class);
        assertEquals(HttpStatus.FOUND, resp.getStatusCode());
        return resp.getHeaders().getLocation().toString();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // ------------------------------------------------------------------ utils

    private static String param(String url, String name) {
        int hash = url.indexOf('#');
        String bare = hash < 0 ? url : url.substring(0, hash);
        int mark = bare.indexOf('?');
        String query = mark < 0 ? URI.create(url).getFragment() : bare.substring(mark + 1);
        if (null == query) return null;
        // the login page is a hash route, so its query may sit inside the fragment
        int inner = query.indexOf('?');
        if (mark < 0 && inner >= 0) query = query.substring(inner + 1);
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

    private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

}
