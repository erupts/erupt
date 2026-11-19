package xyz.erupt.upms.service;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptSiteSetting;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Reads the site settings row and renders it as the patch the frontend lays over
 * {@code window.eruptSiteConfig}: only the keys an administrator filled in, so a blank field keeps
 * the {@code app.js} value. The row is read on every page load before login, hence the short
 * cache; a save evicts it on this node and other nodes catch up within the cache window.
 *
 * @author YuePeng
 * date 2026/11/20
 */
@Service
public class EruptSiteSettingService {

    private static final long CACHE_MILLIS = 30_000;

    @Resource
    private EruptDao eruptDao;

    private final AtomicLong loadedAt = new AtomicLong(0);

    private volatile Map<String, Object> siteConfig = Map.of();

    /** The row, or null when the form was never saved */
    public EruptSiteSetting load() {
        return eruptDao.lambdaQuery(EruptSiteSetting.class).one();
    }

    public void evict() {
        loadedAt.set(0);
    }

    /**
     * The eruptSiteConfig patch; theme keys are nested under {@code theme} as in app.js, and the
     * erupt-app properties the form overrides (watermark) under {@code app}
     */
    public Map<String, Object> siteConfig() {
        if (System.currentTimeMillis() - loadedAt.get() >= CACHE_MILLIS) {
            synchronized (this) {
                if (System.currentTimeMillis() - loadedAt.get() >= CACHE_MILLIS) {
                    siteConfig = this.render(this.load());
                    loadedAt.set(System.currentTimeMillis());
                }
            }
        }
        return siteConfig;
    }

    private Map<String, Object> render(EruptSiteSetting setting) {
        Map<String, Object> config = new LinkedHashMap<>();
        if (null == setting) return config;
        put(config, "title", setting, EruptSiteSetting::getTitle);
        put(config, "desc", setting, EruptSiteSetting::getDescription);
        put(config, "logoText", setting, EruptSiteSetting::getLogoText);
        put(config, "logoPath", setting, EruptSiteSetting::getLogoPath);
        put(config, "logoFoldPath", setting, EruptSiteSetting::getLogoFoldPath);
        put(config, "loginLogoPath", setting, EruptSiteSetting::getLoginLogoPath);
        put(config, "faviconPath", setting, EruptSiteSetting::getFaviconPath);
        put(config, "footerText", setting, EruptSiteSetting::getFooterText);
        put(config, "icpNumber", setting, EruptSiteSetting::getIcpNumber);
        flag(config, "copyright", setting.getCopyright(), "show", "hide");
        // the notice window is checked here, so the cache delays its edges by at most CACHE_MILLIS
        if (StringUtils.isNotBlank(setting.getLoginNotice()) && this.within(setting.getLoginNoticeStart(), setting.getLoginNoticeEnd())) {
            config.put("loginNotice", setting.getLoginNotice().trim());
        }
        Map<String, Object> theme = new LinkedHashMap<>();
        put(theme, "primaryColor", setting, EruptSiteSetting::getPrimaryColor);
        put(theme, "loginLayout", setting, EruptSiteSetting::getLoginLayout);
        put(theme, "loginBackground", setting, EruptSiteSetting::getLoginBackground);
        put(theme, "menuMode", setting, EruptSiteSetting::getMenuMode);
        put(theme, "formPanelMode", setting, EruptSiteSetting::getFormPanelMode);
        if ("auto".equals(setting.getColorScheme())) {
            theme.put("dark", "auto");
        } else {
            flag(theme, "dark", setting.getColorScheme(), "dark", "light");
        }
        flag(theme, "customizable", setting.getAppearanceLock(), "allow", "lock");
        if (!theme.isEmpty()) config.put("theme", theme);
        // erupt-app properties (application configuration) the form overrides, laid over /erupt-app itself
        Map<String, Object> app = new LinkedHashMap<>();
        flag(app, "waterMark", setting.getWaterMark(), "on", "off");
        put(app, "waterMarkContent", setting, EruptSiteSetting::getWaterMarkContent);
        flag(app, "waterMarkDate", setting.getWaterMarkDate(), "on", "off");
        if (!app.isEmpty()) config.put("app", app);
        return config;
    }

    private boolean within(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        return (null == start || !now.isBefore(start)) && (null == end || now.isBefore(end));
    }

    // a two-way choice as a boolean; blank (or anything else) leaves the key out
    private static void flag(Map<String, Object> config, String key, String value, String yes, String no) {
        if (yes.equals(value)) config.put(key, true);
        else if (no.equals(value)) config.put(key, false);
    }

    private static void put(Map<String, Object> config, String key, EruptSiteSetting setting, Function<EruptSiteSetting, String> getter) {
        String value = getter.apply(setting);
        if (StringUtils.isNotBlank(value)) config.put(key, value.trim());
    }

}
