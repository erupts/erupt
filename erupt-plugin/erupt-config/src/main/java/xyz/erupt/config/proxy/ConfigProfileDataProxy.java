package xyz.erupt.config.proxy;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.config.model.ConfigItem;
import xyz.erupt.config.model.ConfigProfile;
import xyz.erupt.config.service.EruptConfigService;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

/**
 * The default profile stays, a profile with items stays, and a change to the profiles the
 * instance reads refreshes the running values.
 *
 * @author YuePeng
 */
@Component
public class ConfigProfileDataProxy implements DataProxy<ConfigProfile> {

    private final EruptDao eruptDao;

    private final EruptConfigService configService;

    public ConfigProfileDataProxy(EruptDao eruptDao, EruptConfigService configService) {
        this.eruptDao = eruptDao;
        this.configService = configService;
    }

    @Override
    public void beforeDelete(ConfigProfile profile) {
        if (ConfigProfile.DEFAULT.equals(profile.getCode())) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("config.profile_default"));
        long items = eruptDao.lambdaQuery(ConfigItem.class).with(ConfigItem::getProfile).eq(ConfigProfile::getId, profile.getId()).with().count();
        if (items > 0) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("config.profile_in_use") + " → " + items);
    }

    @Override
    public void afterUpdate(ConfigProfile profile) {
        configService.refresh();
    }

    @Override
    public void afterDelete(ConfigProfile profile) {
        configService.refresh();
    }

}
