package xyz.erupt.upms.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.service.I18NTranslateService;
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

    @Resource
    private I18NTranslateService i18NTranslateService;

    @Override
    public void beforeAdd(EruptUser eruptUser) {
        this.checkDataLegal(eruptUser);
        if (StringUtils.isBlank(eruptUser.getPasswordA())) {
            throw new EruptApiErrorTip(EruptApiModel.Status.WARNING, "密码必填", EruptApiModel.PromptWay.MESSAGE);
        }
        if (eruptUser.getPasswordA().equals(eruptUser.getPasswordB())) {
            if (eruptUser.getIsMd5()) {
                eruptUser.setPassword(MD5Util.digest(eruptUser.getPasswordA()));
            } else {
                eruptUser.setPassword(eruptUser.getPasswordA());
            }
        } else {
            throw new EruptWebApiRuntimeException(i18NTranslateService.translate("两次密码输入不一致"));
        }
    }

    @Override
    public void beforeUpdate(EruptUser eruptUser) {
        this.checkDataLegal(eruptUser);
    }

    private void checkDataLegal(EruptUser eruptUser) {
        if (eruptUser.getEruptPost() != null && eruptUser.getEruptOrg() == null)
            throw new EruptWebApiRuntimeException("选择岗位时，所属组织必填");
        EruptUser curr = eruptUserService.getCurrentEruptUser();
        if (eruptUser.getIsAdmin()) {
            if (null == curr.getIsAdmin() || !curr.getIsAdmin()) {
                throw new EruptWebApiRuntimeException(i18NTranslateService.translate("当前用户非超管，无法创建超管用户！"));
            }
        }
    }
}
