package xyz.erupt.test.share;

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
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.share.model.EruptShareLink;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.erupt.OrderByModel;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptTokenService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A share link, end to end over HTTP: issued by a logged-in user, opened by nobody in particular,
 * and the session it mints reaching the target and nothing else.
 */
class ShareLinkTest extends EruptApplicationTests {

    private static final String LINKS = EruptShareLink.class.getSimpleName();

    private static final String ORDERS = OrderByModel.class.getSimpleName();

    private static final String PAGE = "share-demo.html";

    private static final String MARK = "share-test";

    @Autowired
    private TestRestTemplate rest;

    @Resource
    private EruptTokenService eruptTokenService;

    @Resource
    private TransactionTemplate transactionTemplate;

    private HttpHeaders issuer;

    @BeforeEach
    void login() {
        this.cleanUp();
        // the issuer holds the share link page, the orders model with its add button, and the demo page
        issuer = this.session(true, "share-issuer");
    }

    @AfterEach
    void cleanUp() {
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
        HttpHeaders visitor = this.headers((String) data.get("token"), ORDERS);

        assertEquals(HttpStatus.OK, this.call(visitor, HttpMethod.GET, "/erupt-api/build/" + ORDERS, null).getStatusCode(), "the form's build");
        ResponseEntity<Map> added = this.call(visitor, HttpMethod.POST, "/erupt-api/data/modify/" + ORDERS, "{\"name\":\"" + MARK + "\",\"sort\":1}");
        assertEquals(HttpStatus.OK, added.getStatusCode());
        assertEquals(true, added.getBody().get("success"), "the visitor may add a record");
        assertEquals(1, eruptDao.lambdaQuery(OrderByModel.class).eq(OrderByModel::getName, MARK).count());

        assertEquals(HttpStatus.FORBIDDEN, this.call(visitor, HttpMethod.POST, "/erupt-api/data/table/" + ORDERS, "{\"pageIndex\":1,\"pageSize\":10}").getStatusCode(), "but not read the table");
        assertEquals(HttpStatus.FORBIDDEN, this.call(visitor, HttpMethod.POST, "/erupt-api/data/modify/" + ORDERS + "/update", "{\"id\":1,\"name\":\"x\"}").getStatusCode(), "nor update");
        assertEquals(HttpStatus.FORBIDDEN, this.call(this.headers((String) data.get("token"), LINKS), HttpMethod.GET, "/erupt-api/build/" + LINKS, null).getStatusCode(), "nor reach another model");

        EruptShareLink counted = eruptDao.lambdaQuery(EruptShareLink.class).eq(EruptShareLink::getCode, link.getCode()).one();
        assertEquals(1, counted.getVisitCount());
        assertNotNull(counted.getLastVisitTime());
    }

    @Test
    void disablingALinkEndsItsSessions() {
        EruptShareLink link = this.issue(this.formLink(null, null));
        String token = (String) ((Map<?, ?>) this.exchange(link.getCode(), null).get("data")).get("token");
        HttpHeaders visitor = this.headers(token, ORDERS);
        assertEquals(HttpStatus.OK, this.call(visitor, HttpMethod.GET, "/erupt-api/build/" + ORDERS, null).getStatusCode());

        Map<String, Object> off = this.call(issuer, HttpMethod.POST, "/erupt-api/data/modify/" + LINKS + "/update",
                "{\"id\":" + link.getId() + ",\"title\":\"" + link.getTitle() + "\",\"type\":\"FORM\",\"erupt\":\"" + ORDERS
                        + "\",\"expireTime\":\"" + this.later(60) + "\",\"enabled\":false}").getBody();
        assertEquals(true, off.get("success"), String.valueOf(off.get("message")));
        EruptShareLink stored = eruptDao.find(EruptShareLink.class, link.getId());
        assertNotNull(stored.getCreateUser(), "the creator survives an update that did not send it");
        assertEquals(link.getCode(), stored.getCode());
        assertEquals(1, stored.getVisitCount(), "nor does an update reset the counter");
        assertEquals(HttpStatus.UNAUTHORIZED, this.call(visitor, HttpMethod.GET, "/erupt-api/build/" + ORDERS, null).getStatusCode(), "the open session is gone");
        assertEquals(false, this.meta(link.getCode()).get("available"));
        assertFalse((Boolean) this.exchange(link.getCode(), null).get("success"), "and no new one is minted");
    }

    @Test
    void visitsAreCappedAndExpiryHonoured() {
        EruptShareLink once = this.issue(this.formLink(null, 1));
        assertTrue((Boolean) this.exchange(once.getCode(), null).get("success"));
        assertFalse((Boolean) this.exchange(once.getCode(), null).get("success"), "the second visit is over the cap");

        String body = "{\"title\":\"" + MARK + " expired\",\"type\":\"FORM\",\"erupt\":\"" + ORDERS + "\",\"expireTime\":\"" + this.later(-1) + "\"}";
        EruptShareLink expired = this.issue(body);
        assertFalse((Boolean) this.exchange(expired.getCode(), null).get("success"));
        assertEquals(HttpStatus.OK, this.rest.getForEntity("/erupt-api/share/meta/not-a-code", String.class).getStatusCode());
        assertEquals(false, this.meta("not-a-code").get("available"));
    }

    @Test
    void aPageLinkReachesItsTemplateOnly() {
        EruptShareLink link = this.issue("{\"title\":\"" + MARK + " page\",\"type\":\"PAGE\",\"path\":\"" + PAGE + "\",\"expireTime\":\"" + this.later(60) + "\"}");
        assertEquals(PAGE, link.getPath(), "the path is stored as the menu names it");
        Map<?, ?> data = (Map<?, ?>) this.exchange(link.getCode(), null).get("data");
        assertEquals("/share/page/" + PAGE, data.get("route"));
        String token = (String) data.get("token");
        ResponseEntity<String> page = this.rest.getForEntity("/erupt-api/tpl/" + PAGE + "?_token=" + token, String.class);
        assertEquals(HttpStatus.OK, page.getStatusCode());
        assertTrue(page.getBody().contains("share page"));
        assertEquals(HttpStatus.FORBIDDEN, this.rest.getForEntity("/erupt-api/tpl/other.html?_token=" + token, String.class).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, this.call(this.headers(token, ORDERS), HttpMethod.GET, "/erupt-api/build/" + ORDERS, null).getStatusCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void aLinkNeverGrantsMoreThanItsIssuerHas() {
        // a user without the orders model may not share it
        HttpHeaders other = this.session(false, "share-other");
        Map<String, Object> refused = this.call(other, HttpMethod.POST, "/erupt-api/data/modify/" + LINKS, this.formLink(null, null)).getBody();
        assertEquals(false, refused.get("success"));
        // nor read links that are not theirs
        this.issue(this.formLink(null, null));
        Map<String, Object> page = this.call(other, HttpMethod.POST, "/erupt-api/data/table/" + LINKS, "{\"pageIndex\":1,\"pageSize\":10}").getBody();
        assertEquals(0L, ((Number) page.get("total")).longValue(), "another user's link list is empty");
        // the issuer's own list names the target in one column, by the model's display name
        Map<String, Object> mine = this.call(issuer, HttpMethod.POST, "/erupt-api/data/table/" + LINKS, "{\"pageIndex\":1,\"pageSize\":10}").getBody();
        List<Map<String, Object>> rows = (List<Map<String, Object>>) mine.get("list");
        assertEquals(1, rows.size());
        assertEquals("OrderBy Demo", rows.get(0).get("target"));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    // A logged-in session of a real user row (the link's creator is read back through it); the
    // issuer holds everything, any other user holds the link page only
    private HttpHeaders session(boolean issuer, String account) {
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
        return this.headers(token, LINKS);
    }

    private HttpHeaders headers(String token, String erupt) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);
        headers.set("erupt", erupt);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String formLink(String password, Integer maxVisits) {
        return "{\"title\":\"" + MARK + " form\",\"type\":\"FORM\",\"erupt\":\"" + ORDERS + "\",\"expireTime\":\"" + this.later(60) + "\""
                + (null == password ? "" : ",\"password\":\"" + password + "\"")
                + (null == maxVisits ? "" : ",\"maxVisits\":" + maxVisits) + "}";
    }

    private EruptShareLink issue(String body) {
        Map<String, Object> resp = this.call(issuer, HttpMethod.POST, "/erupt-api/data/modify/" + LINKS, body).getBody();
        assertEquals(true, resp.get("success"), "issuing must pass: " + resp.get("message"));
        List<EruptShareLink> links = eruptDao.lambdaQuery(EruptShareLink.class).like(EruptShareLink::getTitle, MARK).orderByDesc(EruptShareLink::getId).list();
        EruptShareLink link = links.get(0);
        assertEquals(EruptShareLink.CODE_LENGTH, link.getCode().length());
        assertNull(link.getPassword(), "the password never persists in clear");
        return link;
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

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map> call(HttpHeaders headers, HttpMethod method, String path, String body) {
        return this.rest.exchange(path, method, new HttpEntity<>(body, headers), Map.class);
    }
}
