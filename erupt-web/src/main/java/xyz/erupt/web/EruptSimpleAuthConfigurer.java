package xyz.erupt.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import xyz.erupt.web.base.LoginModel;
import xyz.erupt.web.vo.EruptUserinfoVo;

import javax.servlet.http.HttpServletRequest;

/**
 * 简单认证
 * 使用指定用户名和密码
 * @author fanhang
 */
public class EruptSimpleAuthConfigurer implements EruptWebConfigurer {
    private final Logger log = LoggerFactory.getLogger(EruptSimpleAuthConfigurer.class);
    private final String account;
    private final String pwd;
    private final EruptUserinfoVo userinfo;

    public EruptSimpleAuthConfigurer(String account, String pwd) {
        Assert.hasLength(account, "account must not be null");
        this.account = account.trim();
        this.pwd = pwd.trim();
        this.userinfo = new EruptUserinfoVo();
        userinfo.setNickname(account);
        log.info("simple account: {}", this.account);
    }

    @Override
    public LoginModel login(String account, String pwd, HttpServletRequest request) {
        LoginModel loginModel = new LoginModel();
        log.info("request login: {}", account);
        if (!this.account.equals(account) || this.pwd.equals(pwd)) {
            loginModel.setReason("账号或密码错误");
            loginModel.setPass(false);
            return loginModel;
        }
        loginModel.setPass(true);
        request.getSession().invalidate();
        loginModel.setToken("simple-token");
        loginModel.setResetPwd(false);
        return loginModel;
    }

    @Override
    public EruptUserinfoVo getUserinfo(HttpServletRequest request) {
        return this.userinfo;
    }
}
