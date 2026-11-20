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
    private xyz.erupt.upms.model.data_proxy.EruptSiteSettingDataProxy dataProxy;

    @AfterEach
    void clear() {
        transactionTemplate.executeWithoutResult(s -> eruptDao.lambdaQuery(EruptSiteSetting.class).delete());
        eruptSiteSettingService.evict();
    }

    private Map<String, Object> save(EruptSiteSetting setting) {
        transactionTemplate.executeWithoutResult(s -> eruptDao.persist(setting));
        eruptSiteSettingService.evict();
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
