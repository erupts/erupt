package xyz.erupt.upms.model.data_proxy;

import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptSiteSetting;
import xyz.erupt.upms.service.EruptSiteSettingService;

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
    public void formSave(EruptSiteSetting model) {
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
