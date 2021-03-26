package xyz.erupt.upms.fun;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.upms.model.EruptUser;

/**
 * @author YuePeng
 * date 2021/2/13 20:11
 */
public interface LoginProxy {

    @Comment("登录校验，如要提示校验结果请抛异常")
    @Comment("为安全考虑pwd是加密的，加密逻辑：md5(md5(pwd)+ Calendar.DAY_OF_MONTH +account)")
    @Comment("Calendar.DAY_OF_MONTH → Calendar.getInstance().get(Calendar.DAY_OF_MONTH) //今天月的哪一天")
    @Comment("如果不希望加密，请前往配置文件，将：erupt-app.pwdTransferEncrypt 设置为 false 即可")
    EruptUser login(String account, String pwd);

    @Comment("登录成功")
    default void loginSuccess(EruptUser eruptUser, String token) {
    }

    @Comment("注销事件")
    default void logout(String token) {

    }

    @Comment("修改密码")
    default void beforeChangePwd(EruptUser eruptUser, String newPwd) {

    }

}
