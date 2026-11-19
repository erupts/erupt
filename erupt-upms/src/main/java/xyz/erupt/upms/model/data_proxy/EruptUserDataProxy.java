package xyz.erupt.upms.model.data_proxy;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.view.R;
import xyz.erupt.upms.helper.UpmsSecurityHelper;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.spi.LoginPolicy;

import java.util.Collections;
import java.util.List;

/**
 * @author YuePeng
 * date 2022/1/9 01:02
 */
@Component
public class EruptUserDataProxy implements DataProxy<EruptUser> {

    @Resource
    private EruptUserService eruptUserService;

    // A password set from the admin table goes through the same policies as a self-service change
    @Autowired(required = false)
    private List<LoginPolicy> loginPolicies = Collections.emptyList();

    @Override
    public void validate(EruptUser eruptUser) throws EruptException {
        if (eruptUser.getEruptPost() != null && eruptUser.getEruptOrg() == null)
            throw new EruptException(I18nTranslate.$translate("upms.position_no_org"));
        EruptUser curr = eruptUserService.getCurrentEruptUser();
        if (eruptUser.getIsAdmin()) {
            if (null == curr.getIsAdmin() || !curr.getIsAdmin()) {
                throw new EruptException(I18nTranslate.$translate("upms.not_super_admin-unable_add"));
            }
        }
    }

    @Override
    public void beforeAdd(EruptUser eruptUser) {
        if (StringUtils.isBlank(eruptUser.getPasswordA())) {
            throw new EruptApiErrorTip(R.Status.WARNING, I18nTranslate.$translate("upms.pwd_required"), R.PromptWay.MESSAGE);
        }
        if (eruptUser.getPasswordA().equals(eruptUser.getPasswordB())) {
            for (LoginPolicy policy : loginPolicies) {
                String violation = policy.validateNewPassword(eruptUser, eruptUser.getPasswordA());
                if (null != violation) throw new EruptWebApiRuntimeException(violation);
            }
            UpmsSecurityHelper.applyPassword(eruptUser, eruptUser.getPasswordA(), eruptUser.getEncrypt());
        } else {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.pwd_two_inconsistent"));
        }
    }

}