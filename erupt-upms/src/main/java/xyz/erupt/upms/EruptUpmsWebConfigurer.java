package xyz.erupt.upms;

import com.google.gson.reflect.TypeToken;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.web.EruptWebConfigurer;
import xyz.erupt.web.base.LoginModel;
import xyz.erupt.web.prop.EruptAppProp;
import xyz.erupt.web.vo.EruptMenuVo;
import xyz.erupt.web.vo.EruptUserinfoVo;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.fun.LoginProxy;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * @author fanhang
 */
public class EruptUpmsWebConfigurer implements EruptWebConfigurer {
    private final Type menuListType = new TypeToken<List<EruptMenuVo>>(){}.getType();
    @Resource
    private EruptUserService eruptUserService;
    @Resource
    private EruptSessionService sessionService;
    @Resource
    private EruptAppProp eruptAppProp;
    @Resource
    private EruptContextService eruptContextService;
    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Override
    public LoginModel login(String account, String pwd, HttpServletRequest request) {
        String verifyCode = request.getParameter("verifyCode");
        String verifyCodeMark = request.getParameter("verifyCodeMark");
        if (!eruptUserService.checkVerifyCode(account, verifyCode, verifyCodeMark)) {
            LoginModel loginModel = new LoginModel();
            loginModel.setUseVerifyCode(true);
            loginModel.setReason("验证码错误");
            loginModel.setPass(false);
            return loginModel;
        }
        LoginProxy loginProxy = EruptUserService.findEruptLogin();
        LoginModel loginModel;
        if (null == loginProxy) {
            loginModel = eruptUserService.login(account, pwd);
        } else {
            loginModel = new LoginModel();
            try {
                EruptUser eruptUser = loginProxy.login(account, pwd);
                if (null == eruptUser) {
                    loginModel.setReason("账号或密码错误");
                    loginModel.setPass(false);
                } else {
                    loginModel.setEruptUser(eruptUser);
                    loginModel.setPass(true);
                }
            } catch (Exception e) {
                if (0 == eruptAppProp.getVerifyCodeCount()) {
                    loginModel.setUseVerifyCode(true);
                }
                loginModel.setReason(e.getMessage());
                loginModel.setPass(false);
            }
        }
        if (loginModel.isPass()) {
            request.getSession().invalidate();
            EruptUser eruptUser = (EruptUser) loginModel.getEruptUser();
            loginModel.setToken(Erupts.generateCode(16));
            loginModel.setExpire(this.eruptUserService.getExpireTime());
            loginModel.setResetPwd(null == eruptUser.getResetPwdTime());
            if (null != loginProxy) {
                loginProxy.loginSuccess(eruptUser, loginModel.getToken());
            }
            sessionService.put(SessionKey.TOKEN_OLINE + loginModel.getToken(), eruptUser.getAccount(), eruptUpmsProp.getExpireTimeByLogin());
            eruptUserService.cacheUserInfo(eruptUser, loginModel.getToken());
            eruptUserService.saveLoginLog(eruptUser, loginModel.getToken()); //记录登录日志
        }
        return loginModel;
    }

    @Override
    public EruptApiModel logout(HttpServletRequest request) {
        String token = eruptContextService.getCurrentToken();
        LoginProxy loginProxy = EruptUserService.findEruptLogin();
        Optional.ofNullable(loginProxy).ifPresent(it -> it.logout(token));
        request.getSession().invalidate();
        for (String uk : SessionKey.USER_KEY_GROUP) {
            sessionService.remove(uk + token);
        }
        return EruptApiModel.successApi();
    }

    @Override
    public EruptUserinfoVo getUserinfo(HttpServletRequest request) {
        EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
        EruptUserinfoVo userinfoVo = new EruptUserinfoVo();
        userinfoVo.setNickname(eruptUser.getName());
        userinfoVo.setResetPwd(null == eruptUser.getResetPwdTime());
        Optional.ofNullable(eruptUser.getEruptMenu()).ifPresent(it -> {
            userinfoVo.setIndexMenuType(it.getType());
            userinfoVo.setIndexMenuValue(it.getValue());
        });
        return userinfoVo;
    }

    @Override
    public List<EruptMenuVo> getMenus(HttpServletRequest request) {
        List<EruptMenuVo> menus = sessionService.get(SessionKey.MENU_VIEW + eruptContextService.getCurrentToken(), menuListType);
        menus.forEach(it -> it.setName(I18nTranslate.$translate(it.getName())));
        return menus;
    }
}
