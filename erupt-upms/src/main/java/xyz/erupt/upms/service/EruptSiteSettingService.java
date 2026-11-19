package xyz.erupt.upms.service;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptSiteSetting;

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

    /** The eruptSiteConfig patch; theme keys are nested under {@code theme} as in app.js */
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
        Map<String, Object> theme = new LinkedHashMap<>();
        put(theme, "primaryColor", setting, EruptSiteSetting::getPrimaryColor);
        put(theme, "loginLayout", setting, EruptSiteSetting::getLoginLayout);
        put(theme, "loginBackground", setting, EruptSiteSetting::getLoginBackground);
        if (!theme.isEmpty()) config.put("theme", theme);
        return config;
    }

    private static void put(Map<String, Object> config, String key, EruptSiteSetting setting, Function<EruptSiteSetting, String> getter) {
        String value = getter.apply(setting);
        if (StringUtils.isNotBlank(value)) config.put(key, value.trim());
    }

}
