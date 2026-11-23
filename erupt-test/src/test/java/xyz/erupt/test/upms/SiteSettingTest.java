package xyz.erupt.test.upms;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.model.EruptSiteSetting;
import xyz.erupt.upms.service.EruptSiteSettingService;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The site settings reach the frontend as a patch over app.js: blank fields stay out, two-way
 * choices become booleans, the login notice honours its time window and the watermark lands
 * under "app" for /erupt-app itself.
 */
class SiteSettingTest extends EruptApplicationTests {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private EruptSiteSettingService eruptSiteSettingService;

    @Resource
    private xyz.erupt.upms.service.MaintenanceLoginPolicy maintenancePolicy;

    @Resource
    private xyz.erupt.upms.service.SiteReadOnlyGate readOnlyGate;

    @Resource
    private xyz.erupt.upms.tenant.UpmsTenantSessions tenantSessions;

    @Resource
    private xyz.erupt.upms.model.data_proxy.EruptSiteSettingDataProxy dataProxy;

    @AfterEach
    void clear() {
        transactionTemplate.executeWithoutResult(s -> eruptDao.lambdaQuery(xyz.erupt.upms.model.EruptSiteSettingStore.class).delete());
        eruptSiteSettingService.evict();
    }

    private Map<String, Object> save(EruptSiteSetting setting) {
        eruptSiteSettingService.save(setting);
        return eruptSiteSettingService.siteConfig();
    }

    @Test
    @SuppressWarnings("unchecked")
    void onlyFilledKeysAreDelivered() {
        EruptSiteSetting setting = new EruptSiteSetting();
        setting.setTitle(" Acme ");
        setting.setLogoText("");
        setting.setCopyright("hide");
        setting.setColorScheme("auto");
        setting.setAppearanceLock("lock");
        setting.setWaterMark("on");
        setting.setWaterMarkContent("Internal");
        Map<String, Object> site = save(setting);

        assertEquals("Acme", site.get("title"));
        assertFalse(site.containsKey("logoText"), "a blank field keeps the app.js value");
        assertEquals(false, site.get("copyright"));
        Map<String, Object> theme = (Map<String, Object>) site.get("theme");
        assertEquals("auto", theme.get("dark"));
        assertEquals(false, theme.get("customizable"));
        assertFalse(theme.containsKey("menuMode"));
        Map<String, Object> app = (Map<String, Object>) site.get("app");
        assertEquals(true, app.get("waterMark"));
        assertEquals("Internal", app.get("waterMarkContent"));
        assertFalse(app.containsKey("waterMarkDate"));
    }

    @Test
    void theLoginNoticeHonoursItsWindow() {
        EruptSiteSetting setting = new EruptSiteSetting();
        setting.setLoginNotice("Maintenance tonight");
        setting.setLoginNoticeStart(LocalDateTime.now().plusHours(1));
        assertFalse(save(setting).containsKey("loginNotice"), "not shown before its start");

        clear();
        EruptSiteSetting open = new EruptSiteSetting();
        open.setLoginNotice("Maintenance tonight");
        open.setLoginNoticeStart(LocalDateTime.now().minusHours(1));
        open.setLoginNoticeEnd(LocalDateTime.now().plusHours(1));
        assertTrue(save(open).containsKey("loginNotice"));

        clear();
        EruptSiteSetting expired = new EruptSiteSetting();
        expired.setLoginNotice("Maintenance tonight");
        expired.setLoginNoticeEnd(LocalDateTime.now().minusMinutes(1));
        assertFalse(save(expired).containsKey("loginNotice"), "not shown after its end");
    }

    @Test
    @SuppressWarnings("unchecked")
    void languagesAndLinksReachTheFrontend() {
        EruptSiteSetting setting = new EruptSiteSetting();
        setting.setDefaultLang("zh-CN");
        setting.setLocales(new java.util.HashSet<>(java.util.List.of("zh-CN", "en-US")));
        xyz.erupt.upms.model.EruptSiteLink link = new xyz.erupt.upms.model.EruptSiteLink();
        link.setName("Help");
        link.setUrl("https://docs.example.com");
        setting.setLinks(new java.util.ArrayList<>(java.util.List.of(link)));
        Map<String, Object> site = save(setting);

        assertEquals("zh-CN", site.get("defaultLang"));
        assertEquals(java.util.List.of("en-US", "zh-CN"), ((Map<String, Object>) site.get("app")).get("locales"), "in the configured order");
        assertEquals(1, ((java.util.List<?>) site.get("links")).size());
    }

    // The links are a list inside the stored document, edited as MULTI_FORM blocks: walk the form endpoints' own steps
    // (validate, Gson, formSave, formViewBehavior, data map) and check order and updates survive
    @Test
    @SuppressWarnings("unchecked")
    void headerLinksRoundTripThroughTheFormAsJson() {
        xyz.erupt.core.view.EruptModel eruptModel = xyz.erupt.core.service.EruptCoreService.getErupt(EruptSiteSetting.class.getSimpleName());
        java.util.function.Function<String, java.util.List<Map<String, Object>>> submit = json -> {
            com.google.gson.JsonObject data = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            assertTrue(xyz.erupt.core.util.EruptUtil.validateEruptValue(eruptModel, data).isSuccess());
            dataProxy.formSave(xyz.erupt.core.config.GsonFactory.getGson().fromJson(data, EruptSiteSetting.class));
            EruptSiteSetting view = new EruptSiteSetting();
            dataProxy.formViewBehavior(view);
            return (java.util.List<Map<String, Object>>) xyz.erupt.core.util.EruptUtil.generateEruptDataMap(eruptModel, view, false).get("links");
        };
        java.util.List<Map<String, Object>> first = submit.apply(
                "{\"links\":[{\"name\":\"Help\",\"url\":\"https://docs.example.com\",\"openWay\":\"blank\",\"position\":\"header\"},"
                        + "{\"name\":\"Support\",\"url\":\"mailto:support@example.com\",\"icon\":\"fa fa-envelope\",\"openWay\":\"self\",\"position\":\"header\"}]}");
        assertEquals(java.util.List.of("Help", "Support"), first.stream().map(it -> it.get("name")).toList());
        assertEquals("fa fa-envelope", first.get(1).get("icon"));

        // an update that reorders, edits and drops: the stored list is replaced as a whole, still one row
        java.util.List<Map<String, Object>> second = submit.apply(
                "{\"links\":[{\"name\":\"Desk\",\"url\":\"/desk\",\"openWay\":\"frame\",\"position\":\"login\"},"
                        + "{\"name\":\"Help\",\"url\":\"https://docs.example.com/v2\",\"openWay\":\"blank\",\"position\":\"header\"}]}");
        assertEquals(java.util.List.of("Desk", "Help"), second.stream().map(it -> it.get("name")).toList());
        assertEquals("https://docs.example.com/v2", second.get(1).get("url"));
        assertEquals(1, eruptDao.lambdaQuery(xyz.erupt.upms.model.EruptSiteSettingStore.class).count());
        java.util.List<Map<String, Object>> delivered = (java.util.List<Map<String, Object>>) eruptSiteSettingService.siteConfig().get("links");
        assertEquals(java.util.List.of("Desk", "Help"), delivered.stream().map(it -> it.get("name")).toList(), "in list order");
        assertEquals(java.util.List.of(xyz.erupt.upms.model.EruptSiteLink.FRAME, xyz.erupt.upms.model.EruptSiteLink.BLANK),
                delivered.stream().map(it -> it.get("openWay")).toList());
        assertEquals(java.util.List.of(xyz.erupt.upms.model.EruptSiteLink.LOGIN, xyz.erupt.upms.model.EruptSiteLink.HEADER),
                delivered.stream().map(it -> it.get("position")).toList());
        // a mail link has no page to load inside the app frame
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> submit.apply(
                "{\"links\":[{\"name\":\"Mail\",\"url\":\"mailto:support@example.com\",\"openWay\":\"frame\",\"position\":\"header\"}]}"));

        assertTrue(submit.apply("{\"links\":[]}").isEmpty(), "an emptied list clears the links");
    }

    // One row holds the whole form as a document: nothing but settings in it, a menu by id alone,
    // and what describes the row (owner, audit) in columns of its own
    @Test
    void theFormIsStoredAsOneDocument() {
        xyz.erupt.upms.model.EruptMenu menu = new xyz.erupt.upms.model.EruptMenu();
        menu.setName("Site Home");
        menu.setCode("site-setting-test-home");
        menu.setValue("SiteSettingTestHome");
        transactionTemplate.executeWithoutResult(s -> eruptDao.persist(menu));
        try {
            EruptSiteSetting setting = new EruptSiteSetting();
            setting.setTitle("Acme");
            setting.setLoginNoticeStart(LocalDateTime.of(2026, 1, 2, 3, 4, 5));
            setting.setLocales(new java.util.HashSet<>(java.util.List.of("en-US")));
            setting.setHomeMenu(menu);
            eruptSiteSettingService.save(setting);

            xyz.erupt.upms.model.EruptSiteSettingStore store = eruptDao.lambdaQuery(xyz.erupt.upms.model.EruptSiteSettingStore.class).one();
            assertEquals(xyz.erupt.upms.helper.UpmsTenant.PLATFORM, store.getTenantId(), "the platform's own row");
            assertTrue(null != store.getUpdateTime(), "audit lives in the row");
            com.google.gson.JsonObject document = com.google.gson.JsonParser.parseString(store.getSettings()).getAsJsonObject();
            assertEquals("Acme", document.get("title").getAsString());
            assertEquals(java.util.Set.of("id"), document.getAsJsonObject("homeMenu").keySet(), "a menu is kept by id alone");
            for (String key : java.util.List.of("id", "createBy", "createTime", "updateBy", "updateTime")) {
                assertFalse(document.has(key), key + " is not a setting");
            }

            EruptSiteSetting loaded = eruptSiteSettingService.load();
            assertEquals(LocalDateTime.of(2026, 1, 2, 3, 4, 5), loaded.getLoginNoticeStart());
            assertEquals(java.util.Set.of("en-US"), loaded.getLocales());
            assertEquals("SiteSettingTestHome", loaded.getHomeMenu().getValue(), "resolved from its id");
            assertEquals(store.getUpdateTime(), loaded.getUpdateTime());

            // saving again updates the same row
            setting.setTitle("Acme 2");
            eruptSiteSettingService.save(setting);
            assertEquals(1, eruptDao.lambdaQuery(xyz.erupt.upms.model.EruptSiteSettingStore.class).count());
            assertEquals("Acme 2", eruptSiteSettingService.siteConfig().get("title"));

            // a menu deleted after it was chosen reads as no home menu
            transactionTemplate.executeWithoutResult(s -> eruptDao.lambdaQuery(xyz.erupt.upms.model.EruptMenu.class)
                    .eq(xyz.erupt.upms.model.EruptMenu::getCode, "site-setting-test-home").delete());
            assertEquals(null, eruptSiteSettingService.load().getHomeMenu());
        } finally {
            transactionTemplate.executeWithoutResult(s -> eruptDao.lambdaQuery(xyz.erupt.upms.model.EruptMenu.class)
                    .eq(xyz.erupt.upms.model.EruptMenu::getCode, "site-setting-test-home").delete());
        }
    }

    // Rows are per tenant, the way the user directory is: a tenant session reads and writes its own
    // row, is served the platform's settings until it saves any, and obeys the platform's maintenance
    @Test
    void aTenantOwnsItsOwnSettings() {
        String tenant = "site-setting-test-tenant";
        EruptSiteSetting platform = new EruptSiteSetting();
        platform.setTitle("Platform");
        platform.setMaintenance("on");
        platform.setMaintenanceMessage("Back at 22:00");
        platform.setFooterText("Platform footer");
        platform.getLinks().add(link("Help", "https://docs.example.com"));
        eruptSiteSettingService.save(platform);
        try {
            tenantSessions.runBound(tenant, () -> {
                assertEquals(null, eruptSiteSettingService.load(), "nothing of its own yet");
                assertEquals("Platform", eruptSiteSettingService.siteConfig().get("title"), "the platform's until it saves");

                EruptSiteSetting own = new EruptSiteSetting();
                own.setTitle("Tenant");
                own.setMaintenance("off");
                own.getLinks().add(link("Desk", "https://desk.example.com"));
                eruptSiteSettingService.save(own);
                Map<String, Object> config = eruptSiteSettingService.siteConfig();
                assertEquals("Tenant", config.get("title"));
                assertEquals("Platform footer", config.get("footerText"), "a field the tenant left blank stays the platform's");
                assertEquals(java.util.List.of("Help", "Desk"), ((java.util.List<Map<String, Object>>) config.get("links")).stream()
                        .map(it -> it.get("name")).toList(), "the platform's links stay, the tenant's follow");
                assertEquals(null, eruptSiteSettingService.load().getMaintenance(), "not the tenant's to store");
                assertEquals(tenant, eruptDao.lambdaQuery(xyz.erupt.upms.model.EruptSiteSettingStore.class).one().getTenantId(), "stamped by Hibernate");
                assertTrue(eruptSiteSettingService.maintenanceActive(), "maintenance is the platform's call");
                assertEquals("Back at 22:00", eruptSiteSettingService.siteConfig().get("maintenance"));
            });
            assertEquals("Platform", eruptSiteSettingService.siteConfig().get("title"), "the platform never sees a tenant's row");
            assertEquals(1, eruptDao.lambdaQuery(xyz.erupt.upms.model.EruptSiteSettingStore.class).count());
        } finally {
            tenantSessions.runAs(tenant, em -> eruptDao.lambdaQuery(em, xyz.erupt.upms.model.EruptSiteSettingStore.class).delete());
        }
    }

    // Read-only mode refuses every change through the modify gate; the platform's switch holds for a
    // tenant, which may switch its own on but never the platform's off
    @Test
    void readOnlyModeRefusesEveryChange() {
        String tenant = "site-setting-test-tenant";
        xyz.erupt.core.view.EruptModel model = xyz.erupt.core.service.EruptCoreService.getErupt(EruptSiteSetting.class.getSimpleName());
        assertEquals(null, readOnlyGate.add(model, new com.google.gson.JsonObject()), "off until switched on");
        try {
            tenantSessions.runBound(tenant, () -> {
                EruptSiteSetting own = new EruptSiteSetting();
                own.setReadOnly("on");
                own.setReadOnlyMessage("Stock count in progress");
                eruptSiteSettingService.save(own);
                assertEquals("Stock count in progress", readOnlyGate.update(model, new com.google.gson.JsonObject()));
            });
            assertEquals(null, readOnlyGate.delete(model, java.util.List.of(1L)), "a tenant's switch is its own");

            EruptSiteSetting platform = new EruptSiteSetting();
            platform.setReadOnly("on");
            eruptSiteSettingService.save(platform);
            org.junit.jupiter.api.Assertions.assertNotNull(readOnlyGate.delete(model, java.util.List.of(1L)), "the standard text");
            tenantSessions.runBound(tenant, () -> {
                EruptSiteSetting own = new EruptSiteSetting();
                own.setReadOnly("off");
                eruptSiteSettingService.save(own);
                org.junit.jupiter.api.Assertions.assertNotNull(readOnlyGate.add(model, new com.google.gson.JsonObject()), "a tenant cannot lift the platform's");
            });
        } finally {
            tenantSessions.runAs(tenant, em -> eruptDao.lambdaQuery(em, xyz.erupt.upms.model.EruptSiteSettingStore.class).delete());
        }
    }

    private static xyz.erupt.upms.model.EruptSiteLink link(String name, String url) {
        xyz.erupt.upms.model.EruptSiteLink link = new xyz.erupt.upms.model.EruptSiteLink();
        link.setName(name);
        link.setUrl(url);
        return link;
    }

    // A row left by a schema that kept every setting in a column has no document yet
    @Test
    void aRowWithoutADocumentReadsAsEmptySettings() {
        transactionTemplate.executeWithoutResult(s -> eruptDao.persist(new xyz.erupt.upms.model.EruptSiteSettingStore()));
        eruptSiteSettingService.evict();
        assertTrue(eruptSiteSettingService.siteConfig().isEmpty());
        EruptSiteSetting setting = new EruptSiteSetting();
        setting.setTitle("Acme");
        eruptSiteSettingService.save(setting);
        assertEquals(1, eruptDao.lambdaQuery(xyz.erupt.upms.model.EruptSiteSettingStore.class).count(), "the existing row is reused");
    }

    @Test
    void savingRefusesSettingsTheFrontendCannotHonour() {
        EruptSiteSetting lang = new EruptSiteSetting();
        lang.setDefaultLang("ja-JP");
        lang.setLocales(new java.util.HashSet<>(java.util.List.of("en-US")));
        org.junit.jupiter.api.Assertions.assertThrows(xyz.erupt.annotation.exception.EruptException.class, () -> dataProxy.formSave(lang));

        EruptSiteSetting script = new EruptSiteSetting();
        xyz.erupt.upms.model.EruptSiteLink link = new xyz.erupt.upms.model.EruptSiteLink();
        link.setName("x");
        link.setUrl("javascript:alert(1)");
        script.setLinks(new java.util.ArrayList<>(java.util.List.of(link)));
        org.junit.jupiter.api.Assertions.assertThrows(xyz.erupt.annotation.exception.EruptException.class, () -> dataProxy.formSave(script));

    }

    @Test
    void maintenanceRefusesEveryoneButAdministrators() {
        EruptSiteSetting setting = new EruptSiteSetting();
        setting.setMaintenance("on");
        setting.setMaintenanceMessage("Back at 22:00");
        Map<String, Object> site = save(setting);
        assertEquals("Back at 22:00", site.get("maintenance"));

        xyz.erupt.upms.model.EruptUser user = new xyz.erupt.upms.model.EruptUser();
        user.setIsAdmin(false);
        assertEquals("Back at 22:00", maintenancePolicy.beforeIssueToken(user, null, null));
        user.setIsAdmin(true);
        assertEquals(null, maintenancePolicy.beforeIssueToken(user, null, null), "administrators keep working");
    }

}
