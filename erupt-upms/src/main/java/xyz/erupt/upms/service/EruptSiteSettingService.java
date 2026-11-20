package xyz.erupt.upms.service;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptSiteSetting;
import xyz.erupt.upms.prop.EruptAppProp;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads the site settings row and renders it as the patch the frontend lays over
 * {@code window.eruptSiteConfig}: only the keys an administrator filled in, so a blank field keeps
 * the {@code app.js} value. The row is read on every page load before login, hence the short
 * cache; a save evicts it on this node and other nodes catch up within the cache
 * window. Time-bound keys (the login notice) are evaluated per request from the cached row.
 *
 * @author YuePeng
 * date 2026/11/20
 */
@Service
public class EruptSiteSettingService {

    private static final long CACHE_MILLIS = 30_000;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private EruptAppProp eruptAppProp;

    private final AtomicLong loadedAt = new AtomicLong(0);

    private volatile EruptSiteSetting cached;

    /**
     * The row straight from the database, its header links loaded inside the transaction so the
     * detached copy stays readable later; null when the form was never saved
     */
    public EruptSiteSetting load() {
        return transactionTemplate.execute(status -> {
            EruptSiteSetting row = eruptDao.lambdaQuery(EruptSiteSetting.class).one();
            if (null != row) Hibernate.initialize(row.getLinks());
            return row;
        });
    }

    public void evict() {
        loadedAt.set(0);
    }

    /** The cached row (see {@link #load()}), or null when the form was never saved */
    public EruptSiteSetting setting() {
        if (System.currentTimeMillis() - loadedAt.get() >= CACHE_MILLIS) {
            synchronized (this) {
                if (System.currentTimeMillis() - loadedAt.get() >= CACHE_MILLIS) {
                    cached = this.load();
                    loadedAt.set(System.currentTimeMillis());
                }
            }
        }
        return cached;
    }

    /**
     * The eruptSiteConfig patch; theme keys are nested under {@code theme} as in app.js, and the
     * erupt-app properties the form overrides (watermark, languages) under {@code app}
     */
    public Map<String, Object> siteConfig() {
        return this.render(this.setting());
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
        flag(config, "copyright", setting.getCopyright(), "show", "hide");
        if (StringUtils.isNotBlank(setting.getLoginNotice()) && this.within(setting.getLoginNoticeStart(), setting.getLoginNoticeEnd())) {
            config.put("loginNotice", setting.getLoginNotice().trim());
        }
        Map<String, Object> theme = new LinkedHashMap<>();
        put(theme, "primaryColor", setting, EruptSiteSetting::getPrimaryColor);
        put(theme, "skin", setting, EruptSiteSetting::getSkin);
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
        put(config, "defaultLang", setting, EruptSiteSetting::getDefaultLang);
        if (null != setting.getLinks() && !setting.getLinks().isEmpty()) {
            config.put("links", setting.getLinks().stream().map(link -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", link.getName());
                item.put("url", link.getUrl());
                item.put("icon", link.getIcon());
                item.put("newWindow", !Boolean.FALSE.equals(link.getNewWindow()));
                return item;
            }).toList());
        }
        Map<String, Object> app = new LinkedHashMap<>();
        if (null != setting.getLocales() && !setting.getLocales().isEmpty()) {
            // in the order the configuration lists them, which the language switch keeps
            List<String> offered = Stream.of(eruptAppProp.getLocales()).filter(setting.getLocales()::contains).collect(Collectors.toList());
            setting.getLocales().stream().filter(it -> !offered.contains(it)).sorted().forEach(offered::add);
            app.put("locales", offered);
        }
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
