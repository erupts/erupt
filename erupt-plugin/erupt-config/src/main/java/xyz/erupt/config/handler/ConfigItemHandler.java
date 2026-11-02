package xyz.erupt.config.handler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.config.form.ConfigCopyForm;
import xyz.erupt.config.model.ConfigItem;
import xyz.erupt.config.model.ConfigProfile;
import xyz.erupt.config.service.EruptConfigService;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;

/**
 * Copies the selected keys into another profile.
 *
 * @author YuePeng
 */
@Component
public class ConfigItemHandler implements OperationHandler<ConfigItem, ConfigCopyForm> {

    private final EruptDao eruptDao;

    private final EruptConfigService configService;

    public ConfigItemHandler(EruptDao eruptDao, EruptConfigService configService) {
        this.eruptDao = eruptDao;
        this.configService = configService;
    }

    @Override
    @Transactional
    public String exec(List<ConfigItem> data, ConfigCopyForm form, String[] param) {
        ConfigProfile target = eruptDao.find(ConfigProfile.class, form.getProfile());
        int copied = 0;
        for (ConfigItem row : data) {
            ConfigItem source = eruptDao.find(ConfigItem.class, row.getId());
            if (source.getProfile().getId().equals(target.getId())) continue;
            ConfigItem copy = eruptDao.lambdaQuery(ConfigItem.class)
                    .with(ConfigItem::getProfile).eq(ConfigProfile::getId, target.getId()).with()
                    .eq(ConfigItem::getKey, source.getKey()).list().stream().findFirst().orElseGet(ConfigItem::new);
            copy.setProfile(target);
            copy.setKey(source.getKey());
            copy.setType(source.getType());
            copy.setValue(source.getValue());
            copy.setSecret(source.getSecret());
            copy.setEnabled(source.getEnabled());
            copy.setRemark(source.getRemark());
            eruptDao.merge(copy);
            copied++;
        }
        if (configService.reads(target.getCode())) configService.refresh();
        return ConfigRefreshHandler.message(0 == copied ? I18nTranslate.$translate("config.copied_none")
                : I18nTranslate.$translate("config.copied").replace("{n}", String.valueOf(copied)));
    }

}
