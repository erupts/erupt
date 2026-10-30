package xyz.erupt.test.share;

import com.google.gson.JsonParser;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.context.MetaUser;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.share.model.EruptShareLink;
import xyz.erupt.share.service.EruptShareService;
import xyz.erupt.share.service.ShareScopeGuard;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.erupt.OrderByModel;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptTokenService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A share link end to end: issued by a logged-in user, opened by nobody in particular, and the
 * session it mints reaching the target and nothing else. erupt-test runs without
 * erupt-security, so the issuer and the visitor act through the service layer under a
 * registered context, and what the interceptor would refuse is asked of the scope guard directly.
 */
class ShareLinkTest extends EruptApplicationTests {

    private static final String LINKS = EruptShareLink.class.getSimpleName();

    private static final String ORDERS = OrderByModel.class.getSimpleName();

    private static final String PAGE = "share-demo.html";

    private static final String MARK = "share-test";

    // A logged-in session of a real user row
    private record Login(String token, Long uid, String account) {
    }

    @Autowired
    private TestRestTemplate rest;

    @Resource
    private EruptTokenService eruptTokenService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private EruptModifyService modifyService;

    @Resource
    private EruptService eruptService;

    @Resource
    private ShareScopeGuard guard;

    @Resource
    private EruptShareService eruptShareService;

    private Login issuer;

    @BeforeEach
    void login() {
        this.cleanUp();
        // the issuer holds the share link page, the orders model with its add button, and the demo page
        issuer = this.session(true, "share-issuer");
    }

    @AfterEach
    void cleanUp() {
        MetaContext.remove();
        transactionTemplate.executeWithoutResult(status -> {
            eruptDao.lambdaQuery(EruptShareLink.class).like(EruptShareLink::getTitle, MARK).delete();
            eruptDao.lambdaQuery(OrderByModel.class).eq(OrderByModel::getName, MARK).delete();
            eruptDao.lambdaQuery(EruptUser.class).like(EruptUser::getAccount, MARK).delete();
        });
    }

    @Test
    void aFormLinkOpensTheAddFormAndNothingElse() {
        EruptShareLink link = this.issue(this.formLink("secret", null));
        Map<String, Object> meta = this.meta(link.getCode());
        assertEquals(true, meta.get("available"));
        assertEquals(true, meta.get("requirePassword"));
        assertEquals("share-issuer", meta.get("issuer"));

        assertFalse((Boolean) this.exchange(link.getCode(), "wrong").get("success"), "a wrong password is refused");
        Map<String, Object> session = this.exchange(link.getCode(), "secret");
        assertTrue((Boolean) session.get("success"), String.valueOf(session.get("message")));
        Map<?, ?> data = (Map<?, ?>) session.get("data");
        assertEquals("/share/form/" + ORDERS, data.get("route"));
        String token = (String) data.get("token");

        // the session is the issuer's, with the add right on the orders model and nothing else
        this.as(new Login(token, issuer.uid(), issuer.account()), ORDERS);
        Erupts.powerLegal(this.model(ORDERS), p -> p.isAdd());
        assertThrows(EruptWebApiRuntimeException.class, () -> Erupts.powerLegal(this.model(ORDERS), p -> p.isEdit()), "no edit right");
        modifyService.insertEruptData(this.model(ORDERS), JsonParser.parseString("{\"name\":\"" + MARK + "\",\"sort\":1}").getAsJsonObject());
        assertEquals(1, eruptDao.lambdaQuery(OrderByModel.class).eq(OrderByModel::getName, MARK).count(), "the visitor may add a record");

        // what the security interceptor lets the session call
        assertTrue(this.allowed(token, "/erupt-api/build/" + ORDERS, null));
        assertTrue(this.allowed(token, "/erupt-api/data/modify/" + ORDERS, null));
        assertTrue(this.allowed(token, "/erupt-api/comp/choice-item/" + ORDERS + "/name", null));
        assertTrue(this.allowed(token, "/erupt-api/build/SubForm", ORDERS), "a sub-form vouched for by the shared erupt");
        assertFalse(this.allowed(token, "/erupt-api/data/table/" + ORDERS, null), "but not read the table");
        assertFalse(this.allowed(token, "/erupt-api/data/modify/" + ORDERS + "/update", null), "nor update");
        assertFalse(this.allowed(token, "/erupt-api/build/" + LINKS, null), "nor reach another model");
        assertFalse(this.allowed(token, "/erupt-api/build/SubForm", LINKS), "nor a sub-form of another model");
        assertTrue(this.allowed(issuer.token(), "/erupt-api/data/table/" + ORDERS, null), "a login session is not scoped");

        EruptShareLink counted = eruptDao.lambdaQuery(EruptShareLink.class).eq(EruptShareLink::getCode, link.getCode()).one();
        assertEquals(1, counted.getVisitCount());
        assertNotNull(counted.getLastVisitTime());
    }

    @Test
    void disablingALinkEndsItsSessions() {
        EruptShareLink link = this.issue(this.formLink(null, null));
        String token = (String) ((Map<?, ?>) this.exchange(link.getCode(), null).get("data")).get("token");
        assertTrue(eruptTokenService.tokenExist(token));
        assertNotNull(eruptShareService.scope(token));

        this.as(issuer, LINKS);
        modifyService.updateEruptData(this.model(LINKS), JsonParser.parseString("{\"id\":" + link.getId() + ",\"title\":\"" + link.getTitle()
                + "\",\"type\":\"FORM\",\"erupt\":\"" + ORDERS + "\",\"expireTime\":\"" + this.later(60) + "\",\"enabled\":false}").getAsJsonObject());
        EruptShareLink stored = eruptDao.find(EruptShareLink.class, link.getId());
        assertNotNull(stored.getCreateUser(), "the creator survives an update that did not send it");
        assertEquals(link.getCode(), stored.getCode());
        assertEquals(1, stored.getVisitCount(), "nor does an update reset the counter");
        assertFalse(eruptTokenService.tokenExist(token), "the open session is gone");
        assertNull(eruptShareService.scope(token));
        assertEquals(false, this.meta(link.getCode()).get("available"));
        assertFalse((Boolean) this.exchange(link.getCode(), null).get("success"), "and no new one is minted");
    }

    @Test
    void visitsAreCappedAndExpiryHonoured() {
        EruptShareLink once = this.issue(this.formLink(null, 1));
        assertTrue((Boolean) this.exchange(once.getCode(), null).get("success"));
        assertFalse((Boolean) this.exchange(once.getCode(), null).get("success"), "the second visit is over the cap");

        EruptShareLink expired = this.issue("{\"title\":\"" + MARK + " expired\",\"type\":\"FORM\",\"erupt\":\"" + ORDERS + "\",\"expireTime\":\"" + this.later(-1) + "\"}");
        assertFalse((Boolean) this.exchange(expired.getCode(), null).get("success"));
        assertEquals(HttpStatus.OK, this.rest.getForEntity("/erupt-api/share/meta/not-a-code", String.class).getStatusCode());
        assertEquals(false, this.meta("not-a-code").get("available"));
    }

    @Test
    void aPageLinkReachesItsTemplateOnly() {
        EruptShareLink link = this.issue("{\"title\":\"" + MARK + " page\",\"type\":\"PAGE\",\"path\":\"" + PAGE + "\",\"expireTime\":\"" + this.later(60) + "\"}");
        assertEquals(PAGE, link.getPath());
        Map<?, ?> data = (Map<?, ?>) this.exchange(link.getCode(), null).get("data");
        assertEquals("/share/page/" + PAGE, data.get("route"));
        String token = (String) data.get("token");
        ResponseEntity<String> page = this.rest.getForEntity("/erupt-api/tpl/" + PAGE + "?_token=" + token, String.class);
        assertEquals(HttpStatus.OK, page.getStatusCode());
        assertTrue(page.getBody().contains("share page"));
        assertTrue(this.allowed(token, "/erupt-api/tpl/" + PAGE, null));
        assertFalse(this.allowed(token, "/erupt-api/tpl/other.html", null));
        assertFalse(this.allowed(token, "/erupt-api/build/" + ORDERS, null));
    }

    @Test
    void aLinkNeverGrantsMoreThanItsIssuerHas() {
        // a user without the orders model may not share it
        Login other = this.session(false, "share-other");
        this.as(other, LINKS);
        assertThrows(RuntimeException.class, () -> modifyService.insertEruptData(this.model(LINKS), JsonParser.parseString(this.formLink(null, null)).getAsJsonObject()));
        // nor read links that are not theirs
        this.issue(this.formLink(null, null));
        this.as(other, LINKS);
        assertEquals(0, this.links().getList().size(), "another user's link list is empty");
        // the issuer's own list names the target in one column, by the model's display name
        this.as(issuer, LINKS);
        Page mine = this.links();
        assertEquals(1, mine.getList().size());
        assertEquals("OrderBy Demo", mine.getList().iterator().next().get("target"));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    // A logged-in session of a real user row; the issuer holds everything, any other user the link page only
    private Login session(boolean issuer, String account) {
        EruptUser row = transactionTemplate.execute(status -> {
            EruptUser u = new EruptUser();
            u.setAccount(account + "-" + MARK);
            u.setName(account);
            u.setIsAdmin(false);
            u.setStatus(true);
            eruptDao.persist(u);
            return u;
        });
        MetaUserinfo user = new MetaUserinfo();
        user.setId(row.getId());
        user.setAccount(row.getAccount());
        user.setUsername(account);
        String token = Erupts.generateCode(22);
        MetaMenu links = MetaMenu.createEruptClassMenu(EruptShareLink.class, null, 1);
        MetaMenu orders = MetaMenu.createEruptClassMenu(OrderByModel.class, null, 2);
        List<MetaMenu> menus = issuer ? List.of(links,
                MetaMenu.createSimpleMenu("links-add", "Add", LINKS + "@ADD", links, 1, "button"),
                MetaMenu.createSimpleMenu("links-edit", "Edit", LINKS + "@EDIT", links, 2, "button"),
                orders, MetaMenu.createSimpleMenu("orders-add", "Add", ORDERS + "@ADD", orders, 1, "button"),
                MetaMenu.createSimpleMenu("demo-page", "Demo", PAGE, null, 3, "tpl"))
                : List.of(links, MetaMenu.createSimpleMenu("links-add", "Add", LINKS + "@ADD", links, 1, "button"));
        eruptTokenService.loginToken(user, menus, token, 5);
        return new Login(token, row.getId(), row.getAccount());
    }

    // The context the security interceptor would have registered for a request of this session on this erupt
    private void as(Login login, String erupt) {
        MetaContext.remove();
        MetaContext.register(new MetaUser(login.uid(), login.account(), login.account()));
        MetaContext.registerToken(login.token());
        MetaContext.register(new MetaErupt(erupt, erupt));
    }

    private EruptModel model(String name) {
        return EruptCoreService.getErupt(name);
    }

    // What the security interceptor would ask the guard about this request of a session
    private boolean allowed(String token, String path, String parentErupt) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath(path);
        if (null != parentErupt) request.addHeader(EruptReqHeaderConst.ERUPT_PARENT_HEADER_KEY, parentErupt);
        return guard.allow(token, request, null);
    }

    private String formLink(String password, Integer maxVisits) {
        return "{\"title\":\"" + MARK + " form\",\"type\":\"FORM\",\"erupt\":\"" + ORDERS + "\",\"expireTime\":\"" + this.later(60) + "\""
                + (null == password ? "" : ",\"password\":\"" + password + "\"")
                + (null == maxVisits ? "" : ",\"maxVisits\":" + maxVisits) + "}";
    }

    private EruptShareLink issue(String body) {
        this.as(issuer, LINKS);
        Long id = (Long) modifyService.insertEruptData(this.model(LINKS), JsonParser.parseString(body).getAsJsonObject());
        EruptShareLink link = eruptDao.find(EruptShareLink.class, id);
        assertEquals(EruptShareLink.CODE_LENGTH, link.getCode().length());
        assertNull(link.getPassword(), "the password never persists in clear");
        return link;
    }

    private Page links() {
        TableQuery query = new TableQuery();
        query.setPageIndex(1);
        query.setPageSize(10);
        return eruptService.getEruptData(this.model(LINKS), query, null);
    }

    private String later(int minutes) {
        return new SimpleDateFormat(DateUtil.ISO_8601).format(new Date(System.currentTimeMillis() + minutes * 60_000L));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> meta(String code) {
        return (Map<String, Object>) this.rest.getForEntity("/erupt-api/share/meta/" + code, Map.class).getBody().get("data");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> exchange(String code, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = null == password ? "{}" : "{\"password\":\"" + password + "\"}";
        return this.rest.exchange("/erupt-api/share/exchange/" + code, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class).getBody();
    }
}
