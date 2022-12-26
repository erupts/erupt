package xyz.erupt.upms.model.input;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.service.I18NTranslateService;
import xyz.erupt.core.util.MD5Util;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUser;

import javax.annotation.Resource;
import javax.transaction.Transactional;
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
    private I18NTranslateService i18NTranslateService;

    @Override
    @Transactional
    public String exec(List<EruptUser> data, ResetPassword resetPassword, String[] param) {
        EruptUser eruptUser = data.get(0);
        if (resetPassword.getPassword().equals(resetPassword.getPassword2())) {
            eruptUser.setResetPwdTime(null);
            eruptUser.setIsMd5(resetPassword.getIsMd5());
            if (resetPassword.getIsMd5()) {
                eruptUser.setPassword(MD5Util.digest(resetPassword.getPassword()));
            } else {
                eruptUser.setPassword(resetPassword.getPassword());
            }
            eruptDao.merge(eruptUser);
        } else {
            throw new EruptWebApiRuntimeException(i18NTranslateService.translate("两次密码输入不一致"));
        }
        return null;
    }

}
