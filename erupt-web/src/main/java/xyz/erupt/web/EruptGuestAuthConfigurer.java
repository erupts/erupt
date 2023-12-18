package xyz.erupt.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import xyz.erupt.web.base.LoginModel;
import xyz.erupt.web.vo.EruptUserinfoVo;

import javax.servlet.http.HttpServletRequest;

/**
 * 匿名认证
 * 用户名: guest, 密码: 任意非空字符
 * @author fanhang
 */
public class EruptGuestAuthConfigurer implements EruptWebConfigurer {
    private final Logger log = LoggerFactory.getLogger(EruptGuestAuthConfigurer.class);
    private final String account;
    private final EruptUserinfoVo userinfo;

    public EruptGuestAuthConfigurer() {
        this("guest");
    }

    public EruptGuestAuthConfigurer(String account) {
        Assert.hasLength(account, "Guest account must not be null");
        this.account = account;
        this.userinfo = new EruptUserinfoVo();
        userinfo.setNickname(account);
        log.warn("guest account: {}", this.account);
    }

    @Override
    public LoginModel login(String account, String pwd, HttpServletRequest request) {
        LoginModel loginModel = new LoginModel();
        log.info("request login: {}/{}", account, pwd);
        if (!this.account.equals(account) && StringUtils.hasLength(pwd)) {
            loginModel.setReason("账号或密码错误");
            loginModel.setPass(false);
            return loginModel;
        }
        loginModel.setPass(true);
        request.getSession().invalidate();
        loginModel.setToken("guest-token");
        loginModel.setResetPwd(false);
        return loginModel;
    }

    @Override
    public EruptUserinfoVo getUserinfo(HttpServletRequest request) {
        return this.userinfo;
    }
}
