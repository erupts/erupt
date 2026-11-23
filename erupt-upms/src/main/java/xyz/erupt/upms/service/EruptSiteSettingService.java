package xyz.erupt.upms.service;

import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.upms.handler.PlatformOnly;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptSiteLink;
import xyz.erupt.upms.model.EruptSiteSetting;
import xyz.erupt.upms.model.EruptSiteSettingStore;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.tenant.UpmsTenantIdentifierResolver;
import xyz.erupt.upms.tenant.UpmsTenantSessions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads the stored site settings (a JSON document, see {@link EruptSiteSettingStore}) and renders them as the patch the frontend lays over
 * {@code window.eruptSiteConfig}: only the keys an administrator filled in, so a blank field keeps
 * the {@code app.js} value. The row is read on every page load before login, hence the short
 * cache; a save evicts it on this node and other nodes catch up within the cache
 * window. Time-bound keys (the login notice) are evaluated per request from the cached row.
 * <p>
 * Rows are per tenant (see {@link EruptSiteSettingStore}): a session is served the platform's settings
 * with its tenant's own laid over them field by field, while maintenance mode is always the
 * platform's and holds for everyone.
 *
 * @author YuePeng
 * date 2026/11/20
 */
@Service
public class EruptSiteSettingService {

    private static final long CACHE_MILLIS = 30_000;

    // Fields of the form that describe the row, not the site: kept in its own columns
    private static final List<String> NOT_SETTINGS = List.of(
            LambdaSee.field(EruptSiteSetting::getId), LambdaSee.field(EruptSiteSetting::getCreateBy),
            LambdaSee.field(EruptSiteSetting::getCreateTime), LambdaSee.field(EruptSiteSetting::getUpdateBy),
            LambdaSee.field(EruptSiteSetting::getUpdateTime));

    // Fields only the platform decides, marked on the form by the handler that hides them from a tenant
    private static final Set<String> PLATFORM_ONLY = Stream.of(EruptSiteSetting.class.getDeclaredFields())
            .filter(field -> Optional.ofNullable(field.getAnnotation(EruptField.class))
                    .map(it -> it.edit().ifRender().exprHandler() == PlatformOnly.class).orElse(false))
            .map(Field::getName).collect(Collectors.toUnmodifiableSet());

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private EruptAppProp eruptAppProp;

    @Resource
    private UpmsTenantSessions tenantSessions;

    @Resource
    private UpmsTenantIdentifierResolver tenantResolver;

    // Per tenant: a null setting is a tenant that saved none, cached like any other answer
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    private record Cached(EruptSiteSetting setting, long loadedAt) {
    }

    /**
     * The settings the current tenant saved itself, with the audit fields of their row; null when it
     * never saved the form. A home menu deleted since it was chosen reads as none.
     */
    public EruptSiteSetting load() {
        return this.read(eruptDao.getEntityManager());
    }

    // Hibernate narrows the query to the tenant of the entity manager's session
    private EruptSiteSetting read(EntityManager em) {
        EruptSiteSettingStore store = eruptDao.lambdaQuery(em, EruptSiteSettingStore.class).one();
        if (null == store) return null;
        EruptSiteSetting setting = StringUtils.isBlank(store.getSettings()) ? new EruptSiteSetting()
                : GsonFactory.getGson().fromJson(store.getSettings(), EruptSiteSetting.class);
        setting.setId(store.getId());
        setting.setCreateBy(store.getCreateBy());
        setting.setCreateTime(store.getCreateTime());
        setting.setUpdateBy(store.getUpdateBy());
        setting.setUpdateTime(store.getUpdateTime());
        if (null != setting.getHomeMenu()) {
            Long menuId = setting.getHomeMenu().getId();
            setting.setHomeMenu(null == menuId ? null : em.find(EruptMenu.class, menuId));
        }
        if (null == setting.getLinks()) setting.setLinks(new ArrayList<>());
        return setting;
    }

    /** Writes the settings into the current tenant's row, creating it the first time, and evicts the cache */
    public void save(EruptSiteSetting setting) {
        EruptMenu home = setting.getHomeMenu();
        JsonObject document;
        try {
            // a menu is an entity with relations of its own: only its id belongs in the document
            setting.setHomeMenu(null);
            document = GsonFactory.getGson().toJsonTree(setting).getAsJsonObject();
        } finally {
            setting.setHomeMenu(home);
        }
        NOT_SETTINGS.forEach(document::remove);
        // a tenant's document never carries what only the platform decides
        if (!UpmsTenant.isPlatform(tenantResolver.resolveCurrentTenantIdentifier())) PLATFORM_ONLY.forEach(document::remove);
        if (null != home && null != home.getId()) {
            JsonObject menu = new JsonObject();
            menu.addProperty(LambdaSee.field(EruptMenu::getId), home.getId());
            document.add(LambdaSee.field(EruptSiteSetting::getHomeMenu), menu);
        }
        transactionTemplate.executeWithoutResult(status -> {
            EruptSiteSettingStore store = Optional.ofNullable(eruptDao.lambdaQuery(EruptSiteSettingStore.class).one())
                    .orElseGet(EruptSiteSettingStore::new);
            store.setSettings(document.toString());
            eruptDao.merge(store);
        });
        this.evict();
    }

    public void evict() {
        cache.clear();
    }

    /**
     * The settings in force for the current tenant: the platform's, overridden field by field by
     * what the tenant filled in itself; null when neither saved the form. Cached per tenant.
     */
    public EruptSiteSetting setting() {
        return this.cached(tenantResolver.resolveCurrentTenantIdentifier());
    }

    // Maintenance is the platform's call and holds for every tenant
    private EruptSiteSetting platform() {
        return this.cached(UpmsTenant.PLATFORM);
    }

    private EruptSiteSetting cached(String tenant) {
        Cached hit = cache.get(tenant);
        if (null == hit || System.currentTimeMillis() - hit.loadedAt() >= CACHE_MILLIS) {
            EruptSiteSetting own = tenantSessions.callAs(tenant, this::read);
            hit = new Cached(UpmsTenant.isPlatform(tenant) ? own : overlay(this.platform(), own), System.currentTimeMillis());
            cache.put(tenant, hit);
        }
        return hit.setting();
    }

    /**
     * A tenant's settings laid over the platform's: a field the tenant left blank keeps the platform's
     * value, the header links of both are shown (the platform's first), and a platform-only field
     * is never the tenant's to set.
     */
    private static EruptSiteSetting overlay(EruptSiteSetting platform, EruptSiteSetting own) {
        if (null == own) return platform;
        EruptSiteSetting merged = new EruptSiteSetting();
        ReflectionUtils.doWithLocalFields(EruptSiteSetting.class, field -> {
            if (Modifier.isStatic(field.getModifiers())) return;
            ReflectionUtils.makeAccessible(field);
            Object base = null == platform ? null : field.get(platform);
            Object mine = field.get(own);
            if (PLATFORM_ONLY.contains(field.getName())) {
                field.set(merged, base);
            } else if (base instanceof List<?> first && mine instanceof List<?> second) {
                field.set(merged, Stream.concat(first.stream(), second.stream()).collect(Collectors.toList()));
            } else {
                field.set(merged, filled(mine) ? mine : base);
            }
        });
        return merged;
    }

    private static boolean filled(Object value) {
        if (value instanceof CharSequence text) return StringUtils.isNotBlank(text);
        if (value instanceof Collection<?> items) return !items.isEmpty();
        return null != value;
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
        if (this.maintenanceActive()) config.put("maintenance", this.maintenanceMessage());
        if (null == setting) return config;
        put(config, "title", setting, EruptSiteSetting::getTitle);
        put(config, "desc", setting, EruptSiteSetting::getDescription);
        put(config, "logoText", setting, EruptSiteSetting::getLogoText);
        put(config, "logoPath", setting, EruptSiteSetting::getLogoPath);
        put(config, "logoFoldPath", setting, EruptSiteSetting::getLogoFoldPath);
        put(config, "loginLogoPath", setting, EruptSiteSetting::getLoginLogoPath);
        put(config, "faviconPath", setting, EruptSiteSetting::getFaviconPath);
        if (StringUtils.isNotBlank(setting.getPwaIcon())) config.put("pwa", Map.of("icon", setting.getPwaIcon().trim()));
        put(config, "registerPage", setting, EruptSiteSetting::getRegisterPage);
        put(config, "privacyUrl", setting, EruptSiteSetting::getPrivacyUrl);
        put(config, "termsUrl", setting, EruptSiteSetting::getTermsUrl);
        if (StringUtils.isNotBlank(setting.getPrivacyUrl()) || StringUtils.isNotBlank(setting.getTermsUrl())) {
            flag(config, "requireConsent", setting.getRequireConsent(), "on", "off");
        }
        flag(config, "tabReuse", setting.getTabReuse(), "on", "off");
        put(config, "footerText", setting, EruptSiteSetting::getFooterText);
        flag(config, "copyright", setting.getCopyright(), "show", "hide");
        if (StringUtils.isNotBlank(setting.getLoginNotice()) && this.within(setting.getLoginNoticeStart(), setting.getLoginNoticeEnd())) {
            config.put("loginNotice", setting.getLoginNotice().trim());
        }
        Map<String, Object> theme = new LinkedHashMap<>();
        put(theme, "primaryColor", setting, EruptSiteSetting::getPrimaryColor);
        put(theme, "skin", setting, EruptSiteSetting::getSkin);
        put(theme, "headerColor", setting, EruptSiteSetting::getHeaderColor);
        flag(theme, "compact", setting.getCompact(), "on", "off");
        flag(theme, "menuGroups", setting.getMenuGroups(), "show", "hide");
        put(theme, "loginLayout", setting, EruptSiteSetting::getLoginLayout);
        put(theme, "loginBackground", setting, EruptSiteSetting::getLoginBackground);
        put(theme, "filter", setting, EruptSiteSetting::getPageFilter);
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
                item.put("openWay", StringUtils.defaultIfBlank(link.getOpenWay(), EruptSiteLink.BLANK));
                item.put("position", StringUtils.defaultIfBlank(link.getPosition(), EruptSiteLink.HEADER));
                return item;
            }).toList());
        }
        if (null != setting.getHiddenTools() && !setting.getHiddenTools().isEmpty()) {
            config.put("hiddenTools", setting.getHiddenTools().stream().sorted().toList());
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

    /** Whether maintenance mode is on: only administrators may sign in or keep a session */
    public boolean maintenanceActive() {
        EruptSiteSetting platform = this.platform();
        return null != platform && "on".equals(platform.getMaintenance());
    }

    /** The maintenance text shown to refused users, the standard one when none was written */
    public String maintenanceMessage() {
        EruptSiteSetting platform = this.platform();
        return null != platform && StringUtils.isNotBlank(platform.getMaintenanceMessage())
                ? platform.getMaintenanceMessage().trim() : I18nTranslate.$translate("upms.maintenance_default");
    }

    /**
     * Whether read-only mode is on for the current tenant: the platform's switch holds for everyone,
     * and a tenant may switch its own on, never the platform's off
     */
    public boolean readOnlyActive() {
        return Stream.of(this.platform(), this.setting()).anyMatch(it -> null != it && "on".equals(it.getReadOnly()));
    }

    /** The text shown to a user whose change was refused, the standard one when none was written */
    public String readOnlyMessage() {
        EruptSiteSetting setting = this.setting();
        return null != setting && StringUtils.isNotBlank(setting.getReadOnlyMessage())
                ? setting.getReadOnlyMessage().trim() : I18nTranslate.$translate("upms.read_only_default");
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
