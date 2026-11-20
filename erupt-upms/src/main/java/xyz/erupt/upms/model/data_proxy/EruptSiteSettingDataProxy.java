package xyz.erupt.upms.model.data_proxy;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptSiteLink;
import xyz.erupt.upms.model.EruptSiteSetting;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.service.EruptSiteSettingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Backs the site settings form view: the form loads the single row, saving writes it back
 * (inserting it the first time) and evicts the cached site config. A Spring bean on purpose:
 * erupt injects a DataProxy only when it is one, otherwise it is instantiated bare.
 *
 * @author YuePeng
 * date 2026/11/20
 */
@Component
public class EruptSiteSettingDataProxy implements DataProxy<EruptSiteSetting> {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TransactionTemplate transactionTemplate;

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
                String url = StringUtils.trimToEmpty(link.getUrl()).toLowerCase();
                if (!(url.startsWith("http://") || url.startsWith("https://") || (url.startsWith("/") && !url.startsWith("//"))
                        || url.startsWith("mailto:") || url.startsWith("tel:"))) {
                    throw new EruptException(I18nTranslate.$translate("upms.site_link_url_invalid") + ": " + link.getUrl());
                }
            }
        }
    }

    // the table API stays reachable to a caller holding the add power: never a second row
    @Override
    public void beforeAdd(EruptSiteSetting setting) {
        if (eruptDao.lambdaQuery(EruptSiteSetting.class).count() > 0) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.site_setting_one_row"));
        }
    }

    @Override
    public void formViewBehavior(EruptSiteSetting model) {
        EruptSiteSetting row = eruptSiteSettingService.load();
        if (null != row) BeanUtils.copyProperties(row, model);
    }

    @Override
    public void formSave(EruptSiteSetting model) throws EruptException {
        this.check(model);
        // a form posted without the links clears them; a null would orphan the managed collection
        if (null == model.getLinks()) model.setLinks(new ArrayList<>());
        transactionTemplate.executeWithoutResult(status -> {
            EruptSiteSetting row = eruptSiteSettingService.load();
            model.setId(null == row ? null : row.getId());
            if (null != row) {
                model.setCreateBy(row.getCreateBy());
                model.setCreateTime(row.getCreateTime());
            }
            eruptDao.merge(model);
        });
        eruptSiteSettingService.evict();
    }

}
