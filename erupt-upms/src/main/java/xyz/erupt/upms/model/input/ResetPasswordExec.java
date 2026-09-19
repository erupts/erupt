package xyz.erupt.upms.model.input;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.helper.UpmsSecurityHelper;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptTokenService;

import java.util.List;

/**
 * @author YuePeng
 * date 2022/12/10 14:30
 */
@Component
public class ResetPasswordExec implements OperationHandler<EruptUser, ResetPassword> {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptTokenService eruptTokenService;

    @Resource
    private EruptContextService eruptContextService;

    @Override
    @Transactional
    public String exec(List<EruptUser> data, ResetPassword resetPassword, String[] param) {
        EruptUser eruptUser = data.get(0);
        if (resetPassword.getPassword().equals(resetPassword.getPassword2())) {
            eruptUser.setResetPwdTime(null);
            eruptUser.setEncrypt(resetPassword.getEncrypt());
            UpmsSecurityHelper.applyPassword(eruptUser, resetPassword.getPassword(), resetPassword.getEncrypt());
            eruptDao.merge(eruptUser);
            // The old password no longer proves anything, so neither do the sessions it opened.
            // The operator's own session is kept in case they reset their own account
            eruptTokenService.logoutOtherTokens(eruptUser.getAccount(), eruptContextService.getCurrentToken());
        } else {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.pwd_two_inconsistent"));
        }
        return null;
    }

    @Override
    public ResetPassword eruptFormValue(List<EruptUser> data, ResetPassword resetPassword, String[] param) {
        resetPassword.setEncrypt(data.get(0).getEncrypt());
        resetPassword.setName(data.get(0).getName());
        return OperationHandler.super.eruptFormValue(data, resetPassword, param);
    }
}