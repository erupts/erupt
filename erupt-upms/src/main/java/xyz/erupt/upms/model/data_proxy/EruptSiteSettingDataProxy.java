package xyz.erupt.upms.model.data_proxy;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.upms.model.EruptSiteLink;
import xyz.erupt.upms.model.EruptSiteSetting;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.service.EruptSiteSettingService;

import java.util.Arrays;
import java.util.Collection;

/**
 * Backs the site settings form view: the form loads the stored settings, saving checks them and
 * hands them to {@link EruptSiteSettingService#save}, which also evicts the cached site config. A Spring bean on purpose:
 * erupt injects a DataProxy only when it is one, otherwise it is instantiated bare.
 *
 * @author YuePeng
 * date 2026/11/20
 */
@Component
public class EruptSiteSettingDataProxy implements DataProxy<EruptSiteSetting> {

    @Resource
    private EruptSiteSettingService eruptSiteSettingService;

    @Resource
    private EruptAppProp eruptAppProp;

    // Settings the frontend would act on badly: a default language nobody is offered, a link that
    // runs script
    private void check(EruptSiteSetting model) throws EruptException {
        if (StringUtils.isNotBlank(model.getDefaultLang())) {
            Collection<String> offered = null != model.getLocales() && !model.getLocales().isEmpty()
                    ? model.getLocales() : Arrays.asList(eruptAppProp.getLocales());
            if (!offered.contains(model.getDefaultLang())) {
                throw new EruptException(I18nTranslate.$translate("upms.site_default_lang_not_offered"));
            }
        }
        if (null != model.getLinks()) {
            for (EruptSiteLink link : model.getLinks()) {
                // a page inside the app frame is a document to load: a mail or phone link has none
                if (!safeUrl(link.getUrl(), !EruptSiteLink.FRAME.equals(link.getOpenWay()))) {
                    throw new EruptException(I18nTranslate.$translate("upms.site_link_url_invalid") + ": " + link.getUrl());
                }
            }
        }
        for (String url : new String[]{model.getRegisterPage(), model.getPrivacyUrl(), model.getTermsUrl()}) {
            if (StringUtils.isNotBlank(url) && !safeUrl(url, false)) {
                throw new EruptException(I18nTranslate.$translate("upms.site_link_url_invalid") + ": " + url);
            }
        }
    }

    // http(s) or a path on this site; mailto: and tel: too for a header link. Nothing that runs script
    private static boolean safeUrl(String raw, boolean contact) {
        String url = StringUtils.trimToEmpty(raw).toLowerCase();
        return url.startsWith("http://") || url.startsWith("https://") || (url.startsWith("/") && !url.startsWith("//"))
                || contact && (url.startsWith("mailto:") || url.startsWith("tel:"));
    }

    // the table API stays reachable to a caller holding the add power, but the settings are a form
    // over one stored document, never rows of their own
    @Override
    public void beforeAdd(EruptSiteSetting setting) {
        throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.site_setting_one_row"));
    }

    @Override
    public void formViewBehavior(EruptSiteSetting model) {
        EruptSiteSetting row = eruptSiteSettingService.load();
        if (null != row) BeanUtils.copyProperties(row, model);
    }

    @Override
    public void formSave(EruptSiteSetting model) throws EruptException {
        this.check(model);
        eruptSiteSettingService.save(model);
    }

}
