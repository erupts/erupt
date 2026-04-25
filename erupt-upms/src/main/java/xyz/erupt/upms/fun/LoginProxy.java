package xyz.erupt.upms.fun;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

/**
 * @author YuePeng
 * date 2021/2/13 20:11
 */
public interface LoginProxy {

    @Comment("Login validation — throw an exception to surface a validation message")
    @Comment("For security, pwd is encrypted. Encryption logic: md5(md5(pwd) + Calendar.DAY_OF_MONTH + account)")
    @Comment("Calendar.DAY_OF_MONTH → Calendar.getInstance().get(Calendar.DAY_OF_MONTH) // day of the current month")
    @Comment("To disable encryption, set erupt-app.pwdTransferEncrypt = false in the configuration file")
    default EruptUser login(String account, String pwd) {
        LoginModel loginModel = EruptSpringUtil.getBean(EruptUserService.class).login(account, pwd);
        if (loginModel.isPass()) {
            return loginModel.getEruptUser();
        } else {
            throw new RuntimeException(loginModel.getReason());
        }
    }

    @Comment("Login success")
    default void loginSuccess(EruptUser eruptUser, String token) {
    }

    @Comment("Logout event")
    default void logout(String token) {

    }

    @Comment("Change password")
    default void beforeChangePwd(EruptUser eruptUser, String newPwd) {

    }

    @Comment("Password change completed")
    default void afterChangePwd(EruptUser eruptUser, String originPwd, String newPwd) {
    }

}
