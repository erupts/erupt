package xyz.erupt.test.sso;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.notice.channel.AbstractNoticeChannel;
import xyz.erupt.notice.channel.sso.DingTalkNoticeChannel;
import xyz.erupt.notice.channel.sso.FeishuNoticeChannel;
import xyz.erupt.notice.channel.sso.SlackNoticeChannel;
import xyz.erupt.notice.channel.sso.WeComNoticeChannel;
import xyz.erupt.notice.model.dataproxy.ChannelChoice;
import xyz.erupt.notice.pojo.NoticeMessage;
import xyz.erupt.sso.constant.SsoProviderType;
import xyz.erupt.sso.model.EruptSso;
import xyz.erupt.sso.model.EruptSsoBind;
import xyz.erupt.sso.service.SsoProviderApi;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptUpmsProp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The notification channels that push through an SSO provider row: each one is offered
 * only while a row of its type is enabled, addresses the identifier the binding holds, sends
 * with the row's app credentials, and refuses a user who never signed in through it.
 */
public class SsoNoticeChannelTest extends EruptApplicationTests {

    private static final String CODE = "ut-notice";

    @Resource
    private EruptDao dao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private SsoProviderApi api;

    @Resource
    private FeishuNoticeChannel feishu;

    @Resource
    private DingTalkNoticeChannel dingTalk;

    @Resource
    private WeComNoticeChannel weCom;

    @Resource
    private SlackNoticeChannel slack;

    @Resource
    private ChannelChoice channelChoice;

    private HttpServer stub;

    private String base;

    private final Map<String, JsonObject> seenBody = new ConcurrentHashMap<>();

    private final Map<String, String> seenAuth = new ConcurrentHashMap<>();

    private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

    @BeforeEach
    void startStub() throws Exception {
        stub = HttpServer.create(new InetSocketAddress(0), 0);
        stub.start();
        base = "http://localhost:" + stub.getAddress().getPort();
    }

    @AfterEach
    void cleanUp() {
        stub.stop(0);
        transactionTemplate.executeWithoutResult(status -> {
            EruptSso sso = dao.lambdaQuery(EruptSso.class).eq(EruptSso::getCode, CODE).one();
            if (null == sso) return;
            api.evictAppToken(sso.getId());
            dao.lambdaQuery(EruptSsoBind.class).eq(EruptSsoBind::getSso, sso).list()
                    .forEach(it -> dao.delete(dao.find(EruptSsoBind.class, it.getId())));
            dao.delete(dao.find(EruptSso.class, sso.getId()));
        });
    }

    @Test
    void aChannelIsOfferedOnlyWhileItsProviderRowIsEnabled() {
        assertFalse(feishu.available(), "no Feishu row, no Feishu channel");
        assertTrue(channelChoice.fetch(new String[0]).stream().map(VLModel::getValue).noneMatch(feishu.code()::equals));
        this.provider(SsoProviderType.FEISHU, "Company Feishu");
        assertTrue(feishu.available());
        assertEquals("Feishu (Company Feishu)", feishu.name());
        List<String> offered = channelChoice.fetch(new String[0]).stream().map(VLModel::getValue).toList();
        assertTrue(offered.contains(feishu.code()), offered.toString());
        assertTrue(offered.contains("EruptInternalNotice"), "the built in channel is still there");
    }

    @Test
    void aUserWhoNeverSignedInThroughTheProviderIsRefused() {
        this.provider(SsoProviderType.FEISHU, "Feishu");
        EruptWebApiRuntimeException e = assertThrows(EruptWebApiRuntimeException.class,
                () -> feishu.send(this.defaultUser(), message("Hi", "there", null)));
        assertTrue(e.getMessage().contains(CODE), e.getMessage());
        assertTrue(calls.isEmpty(), "nothing is sent to the provider");
    }

    @Test
    void feishuPostsARichTextToTheOpenIdWithTheTenantToken() {
        this.stub("/open-apis/auth/v3/tenant_access_token/internal", "{\"code\":0,\"tenant_access_token\":\"t-feishu\",\"expire\":7200}");
        this.stub("/open-apis/im/v1/messages", "{\"code\":0,\"msg\":\"success\",\"data\":{\"message_id\":\"om_1\"}}");
        this.provider(SsoProviderType.FEISHU, "Feishu");
        this.bind("ou_open_1");

        feishu.send(this.defaultUser(), message("Deploy done", "v2.3.0 is live", "https://erupt.example.com/#/build/table/Release"));
        feishu.send(this.defaultUser(), message("Again", "second", "/#/relative"));

        assertEquals("app-id", seenBody.get("/open-apis/auth/v3/tenant_access_token/internal").get("app_id").getAsString());
        assertEquals(1, calls.get("/open-apis/auth/v3/tenant_access_token/internal").get(), "the tenant token is cached");
        assertEquals("Bearer t-feishu", seenAuth.get("/open-apis/im/v1/messages"));
        JsonObject sent = seenBody.get("/open-apis/im/v1/messages");
        assertEquals("ou_open_1", sent.get("receive_id").getAsString());
        assertEquals("post", sent.get("msg_type").getAsString());
        JsonObject post = JsonParser.parseString(sent.get("content").getAsString()).getAsJsonObject().getAsJsonObject("zh_cn");
        assertEquals("Again", post.get("title").getAsString());
        assertEquals(1, post.getAsJsonArray("content").size(), "a relative URL gets no link line");
    }

    @Test
    void dingTalkLooksTheCorpUserIdUpOnceAndSendsAWorkNotification() {
        this.stub("/v1.0/oauth2/accessToken", "{\"accessToken\":\"t-ding\",\"expireIn\":7200}");
        this.stub("/topapi/user/getbyunionid", "{\"errcode\":0,\"result\":{\"userid\":\"manager01\",\"contact_type\":0}}");
        this.stub("/topapi/message/corpconversation/asyncsend_v2", "{\"errcode\":0,\"task_id\":1}");
        this.provider(SsoProviderType.DINGTALK, "DingTalk");
        this.bind("union-1");

        dingTalk.send(this.defaultUser(), message("Approval", "please review", "https://erupt.example.com/#/x"));
        dingTalk.send(this.defaultUser(), message("Approval", "again", null));

        assertEquals("app-id", seenBody.get("/v1.0/oauth2/accessToken").get("appKey").getAsString());
        assertEquals(1, calls.get("/topapi/user/getbyunionid").get(), "the unionId to userid lookup is remembered");
        assertEquals("union-1", seenBody.get("/topapi/user/getbyunionid").get("unionid").getAsString());
        JsonObject sent = seenBody.get("/topapi/message/corpconversation/asyncsend_v2");
        assertEquals("1000002", sent.get("agent_id").getAsString());
        assertEquals("manager01", sent.get("userid_list").getAsString());
        assertEquals("markdown", sent.getAsJsonObject("msg").get("msgtype").getAsString());
        assertEquals(2, calls.get("/topapi/message/corpconversation/asyncsend_v2").get());
    }

    @Test
    void weComSendsATextCardWithALinkAndPlainTextWithout() {
        this.stub("/cgi-bin/gettoken", "{\"errcode\":0,\"access_token\":\"t-wecom\",\"expires_in\":7200}");
        this.stub("/cgi-bin/message/send", "{\"errcode\":0,\"errmsg\":\"ok\"}");
        this.provider(SsoProviderType.WECOM, "WeCom");
        this.bind("zhangsan");

        weCom.send(this.defaultUser(), message("Ticket", "assigned to you", "https://erupt.example.com/#/t/1"));
        JsonObject card = seenBody.get("/cgi-bin/message/send");
        assertEquals("textcard", card.get("msgtype").getAsString());
        assertEquals("zhangsan", card.get("touser").getAsString());
        assertEquals("1000002", card.get("agentid").getAsString());
        assertEquals("https://erupt.example.com/#/t/1", card.getAsJsonObject("textcard").get("url").getAsString());

        weCom.send(this.defaultUser(), message("Ticket", "closed", null));
        JsonObject text = seenBody.get("/cgi-bin/message/send");
        assertEquals("text", text.get("msgtype").getAsString());
        assertTrue(text.getAsJsonObject("text").get("content").getAsString().startsWith("Ticket\n"));
        assertEquals(1, calls.get("/cgi-bin/gettoken").get(), "the corp token is cached");
    }

    @Test
    void weComWithoutAMessagingKeyFailsBeforeCallingTheProvider() {
        this.stub("/cgi-bin/gettoken", "{\"errcode\":0,\"access_token\":\"t-wecom\",\"expires_in\":7200}");
        this.provider(SsoProviderType.WECOM, "WeCom");
        this.configure(sso -> sso.setMessagingKey(null));
        this.bind("zhangsan");
        EruptWebApiRuntimeException e = assertThrows(EruptWebApiRuntimeException.class,
                () -> weCom.send(this.defaultUser(), message("x", "y", null)));
        assertTrue(e.getMessage().contains(CODE), e.getMessage());
        assertFalse(calls.containsKey("/cgi-bin/message/send"));
    }

    @Test
    void slackPostsWithTheBotTokenAndReportsAnOkFalseAnswer() {
        this.stub("/api/chat.postMessage", "{\"ok\":true,\"channel\":\"D1\",\"ts\":\"1\"}");
        this.provider(SsoProviderType.SLACK, "Slack");
        this.bind("U0123");

        slack.send(this.defaultUser(), message("Build", "passed", "https://ci.example.com/1"));
        assertEquals("Bearer xoxb-bot", seenAuth.get("/api/chat.postMessage"));
        JsonObject sent = seenBody.get("/api/chat.postMessage");
        assertEquals("U0123", sent.get("channel").getAsString());
        assertTrue(sent.get("text").getAsString().contains("<https://ci.example.com/1|"), sent.toString());

        this.stub("/api/chat.postMessage", "{\"ok\":false,\"error\":\"channel_not_found\"}");
        EruptWebApiRuntimeException e = assertThrows(EruptWebApiRuntimeException.class,
                () -> slack.send(this.defaultUser(), message("Build", "failed", null)));
        assertTrue(e.getMessage().contains("channel_not_found"), e.getMessage());

        this.configure(sso -> sso.setMessagingKey(null));
        assertThrows(EruptWebApiRuntimeException.class, () -> slack.send(this.defaultUser(), message("x", "y", null)),
                "no messaging key, no message");
    }

    @Test
    void anAppTokenIsOnlyIssuedForProvidersWithAnApplicationApi() {
        this.provider(SsoProviderType.GITHUB, "GitHub");
        EruptSso sso = dao.lambdaQuery(EruptSso.class).eq(EruptSso::getCode, CODE).one();
        assertThrows(EruptWebApiRuntimeException.class, () -> api.appAccessToken(sso));
    }

    // --------------------------------------------------------------- fixtures

    private void provider(SsoProviderType type, String name) {
        SsoProviderType.Preset preset = type.preset();
        transactionTemplate.executeWithoutResult(status -> {
            EruptSso sso = new EruptSso();
            sso.setType(type);
            sso.setCode(CODE);
            sso.setName(name);
            sso.setStatus(true);
            sso.setSort(0);
            // the endpoints the presets derive the API base from, pointed at the stand-in server
            sso.setIssuer(base);
            sso.setAuthorizeUrl(base + "/authorize");
            sso.setTokenUrl(base + (type == SsoProviderType.DINGTALK ? "/v1.0/oauth2/userAccessToken"
                    : type == SsoProviderType.WECOM ? "/cgi-bin/gettoken" : "/token"));
            sso.setUserInfoUrl(base + (type == SsoProviderType.FEISHU ? "/open-apis/authen/v1/user_info"
                    : type == SsoProviderType.WECOM ? "/cgi-bin/user/get" : "/userinfo"));
            sso.setClientId("app-id");
            sso.setClientSecret("app-secret");
            sso.setMessagingKey(type == SsoProviderType.SLACK ? "xoxb-bot" : "1000002");
            sso.setScopes(preset.getScopes());
            sso.setAccountClaim(preset.getAccountClaim());
            sso.setNameClaim(preset.getNameClaim());
            sso.setEmailClaim(preset.getEmailClaim());
            sso.setOpenIdClaim(preset.getOpenIdClaim());
            sso.setAutoCreate(false);
            sso.setSyncProfile(false);
            dao.persist(sso);
        });
    }

    private void configure(java.util.function.Consumer<EruptSso> change) {
        transactionTemplate.executeWithoutResult(status -> {
            EruptSso sso = dao.lambdaQuery(EruptSso.class).eq(EruptSso::getCode, CODE).one();
            change.accept(sso);
            dao.merge(sso);
        });
    }

    private void bind(String openId) {
        transactionTemplate.executeWithoutResult(status -> {
            EruptSsoBind bind = new EruptSsoBind();
            bind.setSso(dao.lambdaQuery(EruptSso.class).eq(EruptSso::getCode, CODE).one());
            bind.setEruptUser(this.defaultUser());
            bind.setSubject("subject-" + openId);
            bind.setOpenId(openId);
            dao.persist(bind);
        });
    }

    private EruptUser defaultUser() {
        return dao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, eruptUpmsProp.getDefaultAccount()).one();
    }

    private static NoticeMessage message(String title, String content, String url) {
        NoticeMessage message = new NoticeMessage();
        message.setTitle(title);
        message.setContent(content);
        message.setUrl(url);
        return message;
    }

    private void stub(String path, String body) {
        calls.putIfAbsent(path, new AtomicInteger());
        try {
            stub.removeContext(path);
        } catch (IllegalArgumentException ignored) {
            // first registration
        }
        stub.createContext(path, exchange -> {
            calls.get(path).incrementAndGet();
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            if (null != auth) seenAuth.put(path, auth);
            String posted = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (!posted.isBlank()) seenBody.put(path, JsonParser.parseString(posted).getAsJsonObject());
            respond(exchange, body);
        });
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
