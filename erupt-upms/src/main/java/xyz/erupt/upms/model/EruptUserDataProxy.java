package xyz.erupt.upms.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.MD5Util;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2022/1/9 01:02
 */
@Component
public class EruptUserDataProxy implements DataProxy<EruptUser> {

    @Resource
    private EruptUserService eruptUserService;

    @Override
    public void beforeAdd(EruptUser eruptUser) {
        this.checkDataLegal(eruptUser);
        if (StringUtils.isBlank(eruptUser.getPasswordA())) {
            throw new EruptApiErrorTip(EruptApiModel.Status.WARNING, I18nTranslate.$translate("upms.pwd_required"), EruptApiModel.PromptWay.MESSAGE);
        }
        if (eruptUser.getPasswordA().equals(eruptUser.getPasswordB())) {
            if (eruptUser.getIsMd5()) {
                eruptUser.setPassword(MD5Util.digest(eruptUser.getPasswordA()));
            } else {
                eruptUser.setPassword(eruptUser.getPasswordA());
            }
        } else {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.pwd_two_inconsistent"));
        }
    }

    @Override
    public void beforeUpdate(EruptUser eruptUser) {
        this.checkDataLegal(eruptUser);
    }

    private void checkDataLegal(EruptUser eruptUser) {
        if (eruptUser.getEruptPost() != null && eruptUser.getEruptOrg() == null)
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.position_no_org"));
        EruptUser curr = eruptUserService.getCurrentEruptUser();
        if (eruptUser.getIsAdmin()) {
            if (null == curr.getIsAdmin() || !curr.getIsAdmin()) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.not_super_admin-unable_add"));
            }
        }
    }
}
