package xyz.erupt.upms.controller;

import com.google.gson.reflect.TypeToken;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.util.SecretUtil;
import xyz.erupt.core.view.R;
import xyz.erupt.upms.base.ChangePwdBody;
import xyz.erupt.upms.base.LoginBody;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.fun.LoginProxy;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptTokenService;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.vo.EruptMenuVo;
import xyz.erupt.upms.vo.EruptUserinfoVo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

;

/**
 * @author YuePeng
 * date 2018-12-13.
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API)
public class EruptUserController {

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptSessionService sessionService;

    @Resource
    private EruptAppProp eruptAppProp;

    @Resource
    private EruptContextService eruptContextService;

    @Resource
    private EruptTokenService eruptTokenService;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    /**
     * Login
     *
     * @param verifyCode     Verification code
     * @param verifyCodeMark Verification code identifier
     */
    @SneakyThrows
    @PostMapping(value = "/login")
    public LoginModel login(@RequestBody LoginBody loginBody) {
        String account = loginBody.getAccount();
        String pwd = loginBody.getPwd();
        if (!eruptUserService.checkVerifyCode(account, loginBody.getVerifyCode(), loginBody.getVerifyCodeMark())) {
            return new LoginModel(false, I18nTranslate.$translate("upms.verify_code_error"), true);
        }
        if (eruptAppProp.getPwdTransferEncrypt()) {
            pwd = SecretUtil.decodeSecret(pwd, 3);
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
                    loginModel.setReason(I18nTranslate.$translate("upms.account_pwd_error"));
                    loginModel.setPass(false);
                } else {
                    loginModel.setEruptUser(eruptUser);
                    loginModel.setPass(true);
                }
            } catch (Exception e) {
                if (0 == eruptAppProp.getVerifyCodeCount()) loginModel.setUseVerifyCode(true);
                loginModel.setReason(e.getMessage());
                loginModel.setPass(false);
            }
        }
        if (loginModel.isPass()) {
            EruptUser eruptUser = loginModel.getEruptUser();
            loginModel.setToken(Erupts.generateCode(16));
            loginModel.setExpire(LocalDateTime.now().plusMinutes(eruptUpmsProp.getExpireTimeByLogin()));
            loginModel.setResetPwd(null == eruptUser.getResetPwdTime());
            if (null != loginProxy) loginProxy.loginSuccess(eruptUser, loginModel.getToken());
            eruptTokenService.loginToken(eruptUser, loginModel.getToken());
            eruptUserService.saveLoginLog(eruptUser, loginModel.getToken()); //Record login log
        }
        return loginModel;
    }

    @PostMapping(value = "/change-pwd")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<Void> changePwd(@RequestBody ChangePwdBody body) {
        String pwd = SecretUtil.decodeSecret(body.getPwd(), 3);
        String newPwd = SecretUtil.decodeSecret(body.getNewPwd(), 3);
        String newPwd2 = SecretUtil.decodeSecret(body.getNewPwd2(), 3);
        return eruptUserService.changePwd(eruptUserService.getCurrentAccount(), pwd, newPwd, newPwd2);
    }

    @GetMapping("/userinfo")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptUserinfoVo userinfo() {
        EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
        EruptUserinfoVo userinfoVo = new EruptUserinfoVo();
        userinfoVo.setNickname(eruptUser.getName());
        userinfoVo.setAvatar(eruptUser.getAvatar());
        userinfoVo.setResetPwd(null == eruptUser.getResetPwdTime());
        Optional.ofNullable(eruptUser.getEruptOrg()).ifPresent(it -> userinfoVo.setOrg(it.getCode()));
        Optional.ofNullable(eruptUser.getEruptPost()).ifPresent(it -> userinfoVo.setPost(it.getCode()));
        Optional.ofNullable(eruptUser.getRoles()).ifPresent(it -> userinfoVo.setRoles(it.stream().map(EruptRole::getCode).collect(Collectors.toList())));
        Optional.ofNullable(eruptUser.getEruptMenu()).ifPresent(it -> {
            userinfoVo.setIndexMenuType(it.getType());
            userinfoVo.setIndexMenuValue(it.getValue());
        });
        return userinfoVo;
    }

    @GetMapping("/menu")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public List<EruptMenuVo> getMenu() {
        List<EruptMenuVo> menus = sessionService.get(SessionKey.MENU_VIEW + eruptContextService.getCurrentToken(), new TypeToken<List<EruptMenuVo>>() {
        }.getType());
        menus.forEach(it -> it.setName(I18nTranslate.$translate(it.getName())));
        return menus;
    }

    @GetMapping(value = "/logout")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<Void> logout() {
        String token = eruptContextService.getCurrentToken();
        MetaUserinfo metaUserinfo = eruptUserService.getSimpleUserInfo();
        LoginProxy loginProxy = EruptUserService.findEruptLogin();
        Optional.ofNullable(loginProxy).ifPresent(it -> it.logout(token));
        eruptTokenService.logoutToken(metaUserinfo.getUsername(), token);
        return R.ok();
    }

    /**
     * Generate verification code
     *
     * @param mark   Generate the verification code marking value
     * @param height Height of the verification code
     */
    @GetMapping("/code-img")
    public void createCode(HttpServletResponse response, @RequestParam long mark,
                           @RequestParam(required = false, defaultValue = "38") Integer height) throws Exception {
        // Clamp height to a sane captcha range to prevent resource abuse via oversized images
        height = Math.max(20, Math.min(height, 100));
        response.setContentType("image/jpeg"); // Set the response type format to image format
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache"); // Prohibit image caching
        response.setHeader("Cache-Control", "no-cache");
        Captcha captcha = new SpecCaptcha(150, height, 4);
        sessionService.put(SessionKey.VERIFY_CODE + mark, captcha.text(), 60, TimeUnit.SECONDS);
        captcha.out(response.getOutputStream());
    }


}
