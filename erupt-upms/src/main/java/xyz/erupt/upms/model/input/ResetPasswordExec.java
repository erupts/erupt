package xyz.erupt.upms.model.input;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.MD5Util;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUser;

import java.util.List;

/**
 * @author YuePeng
 * date 2022/12/10 14:30
 */
@Component
public class ResetPasswordExec implements OperationHandler<EruptUser, ResetPassword> {

    @Resource
    private EruptDao eruptDao;

    @Override
    @Transactional
    public String exec(List<EruptUser> data, ResetPassword resetPassword, String[] param) {
        EruptUser eruptUser = data.get(0);
        if (resetPassword.getPassword().equals(resetPassword.getPassword2())) {
            eruptUser.setResetPwdTime(null);
            // 更新为使用SHA512+盐加密，忽略前端传递的isMd5值
            String salt = MD5Util.generateSalt();
            eruptUser.setSalt(salt);
            eruptUser.setEncryptType("SHA512");
            eruptUser.setPassword(MD5Util.digestSHA512Salt(resetPassword.getPassword(), salt));
            eruptUser.setIsMd5(false); // 设置为false表示不再使用MD5
            eruptDao.merge(eruptUser);
        } else {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.pwd_two_inconsistent"));
        }
        return null;
    }

    @Override
    public ResetPassword eruptFormValue(List<EruptUser> data, ResetPassword resetPassword, String[] param) {
        resetPassword.setIsMd5(data.get(0).getIsMd5());
        return OperationHandler.super.eruptFormValue(data, resetPassword, param);
    }
}