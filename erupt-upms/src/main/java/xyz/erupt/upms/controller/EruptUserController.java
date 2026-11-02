package xyz.erupt.upms.controller;

import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.util.SecretUtil;
import xyz.erupt.core.view.R;
import xyz.erupt.upms.base.ChangePwdBody;
import xyz.erupt.upms.base.VerifyPwdBody;
import xyz.erupt.upms.base.LoginBody;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.base.MfaBody;
import xyz.erupt.upms.base.ProfileBody;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.fun.LoginProxy;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.service.*;
import xyz.erupt.upms.vo.EruptMenuVo;
import xyz.erupt.upms.vo.EruptUserinfoVo;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Session and user
 *
 * @order 1
 * @apiNote Session lifecycle and the signed-in user: login, logout, profile, menu.
 *
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
    private EruptMenuService eruptMenuService;

    @Resource
    private EruptMfaService eruptMfaService;

    /**
     * Sign in with account and password.
     *
     * @apiNote When erupt-app.pwd-transfer-encrypt is on the password is sent base64-encoded three times. After repeated failures useVerifyCode turns true and the captcha from code-img must be sent as verifyCode/verifyCodeMark. When the user has an authenticator bound, pass is false, mfaRequired is true and the returned mfaTicket must be exchanged through login-mfa.
     * @param loginBody Account, password and optional captcha
     * @return token and user info when pass is true, otherwise the reason
     */
    @SneakyThrows
    @PostMapping(value = "/login")
    public LoginModel login(@RequestBody LoginBody loginBody) {
        String account = loginBody.getAccount();
        String pwd = loginBody.getPwd();
        // A locked pair is told so at once, whatever else the request carries
        if (eruptUserService.isLoginLocked(account)) return eruptUserService.lockedLoginModel();
        if (!eruptUserService.checkVerifyCode(account, loginBody.getVerifyCode(), loginBody.getVerifyCodeMark())) {
            return eruptUserService.loginFailure(account, "upms.verify_code_error");
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
            // The password is only the first factor: when an authenticator is bound no session
            // is created here, the caller has to come back through /login-mfa with a code
            if (eruptMfaService.isBound(loginModel.getEruptUser())) {
                loginModel.setPass(false);
                loginModel.setMfaRequired(true);
                loginModel.setMfaTicket(eruptMfaService.issueTicket(loginModel.getEruptUser()));
                return loginModel;
            }
            eruptUserService.completeLogin(loginModel, loginProxy);
        }
        return loginModel;
    }

    /**
     * Second step of a two factor login.
     *
     * @apiNote Exchanges the mfaTicket from login plus a one-time code (or a recovery code) for a session. An ordinary wrong code keeps the ticket alive.
     * @param body mfaTicket and code
     * @return token and user info when pass is true, otherwise the reason
     */
    @PostMapping(value = "/login-mfa")
    public LoginModel loginMfa(@RequestBody MfaBody body) {
        EruptUser eruptUser = eruptMfaService.findUserByTicket(body.getMfaTicket());
        if (null == eruptUser) {
            return new LoginModel(false, I18nTranslate.$translate("upms.mfa.ticket_expired"));
        }
        try {
            eruptMfaService.verifyForLogin(eruptUser, body.getMfaTicket(), body.getCode());
        } catch (Exception e) {
            LoginModel fail = new LoginModel(false, e.getMessage());
            // the ticket survives an ordinary wrong code, so the client stays on the code screen
            if (null != eruptMfaService.findUserByTicket(body.getMfaTicket())) {
                fail.setMfaRequired(true);
                fail.setMfaTicket(body.getMfaTicket());
            }
            return fail;
        }
        LoginModel loginModel = new LoginModel(true, eruptUser);
        eruptUserService.completeLogin(loginModel, EruptUserService.findEruptLogin());
        return loginModel;
    }

    /**
     * Change the current user's password.
     *
     * @apiNote Passwords follow the same transfer encoding as login.
     * @param body Current password and the new password twice
     * @return ok when changed
     */
    @PostMapping(value = "/change-pwd")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<Void> changePwd(@RequestBody ChangePwdBody body) {
        String pwd = eruptAppProp.getPwdTransferEncrypt() ? SecretUtil.decodeSecret(body.getPwd(), 3) : body.getPwd();
        String newPwd = eruptAppProp.getPwdTransferEncrypt() ? SecretUtil.decodeSecret(body.getNewPwd(), 3) : body.getNewPwd();
        String newPwd2 = eruptAppProp.getPwdTransferEncrypt() ? SecretUtil.decodeSecret(body.getNewPwd2(), 3) : body.getNewPwd2();
        return eruptUserService.changePwd(eruptUserService.getCurrentAccount(), pwd, newPwd, newPwd2);
    }

    /**
     * Re-check the current user's password.
     *
     * @apiNote Used to unlock the screen; no new session is issued. It goes through the same path as a sign-in, so the failure counter and lock apply.
     * @param body Password
     * @return ok when it matches
     */
    @PostMapping(value = "/verify-pwd")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<Void> verifyPwd(@RequestBody VerifyPwdBody body) {
        String pwd = eruptAppProp.getPwdTransferEncrypt() ? SecretUtil.decodeSecret(body.getPwd(), 3) : body.getPwd();
        return eruptUserService.verifyPwd(pwd);
    }

    /**
     * Update the current user's profile.
     *
     * @param body Editable profile fields
     * @return ok when saved
     */
    @PostMapping("/profile")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<Void> updateProfile(@RequestBody ProfileBody body) {
        eruptUserService.updateProfile(body);
        return R.ok();
    }

    /**
     * Upload the current user's avatar.
     *
     * @param file Multipart form field "file"
     * @return stored avatar path
     */
    @PostMapping("/profile/avatar")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return R.ok(eruptUserService.uploadAvatar(file));
    }

    /**
     * Current user.
     *
     * @return account, nickname, avatar, org, post, roles, the index menu, when the password was last changed and whether MFA is bound
     */
    @GetMapping("/userinfo")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptUserinfoVo userinfo() {
        EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
        EruptUserinfoVo userinfoVo = new EruptUserinfoVo();
        userinfoVo.setAccount(eruptUser.getAccount());
        userinfoVo.setNickname(eruptUser.getName());
        userinfoVo.setAvatar(eruptUser.getAvatar());
        userinfoVo.setResetPwd(null == eruptUser.getResetPwdTime());
        userinfoVo.setPwdChangedTime(eruptUser.getResetPwdTime());
        userinfoVo.setMfaBound(Boolean.TRUE.equals(eruptUser.getMfaEnabled()));
        Optional.ofNullable(eruptUser.getEruptOrg()).ifPresent(it -> userinfoVo.setOrg(it.getCode()));
        Optional.ofNullable(eruptUser.getEruptPost()).ifPresent(it -> userinfoVo.setPost(it.getCode()));
        Optional.ofNullable(eruptUser.getRoles()).ifPresent(it -> userinfoVo.setRoles(it.stream().map(EruptRole::getCode).collect(Collectors.toList())));
        Optional.ofNullable(eruptUser.getEruptMenu()).ifPresent(it -> {
            userinfoVo.setIndexMenuType(it.getType());
            userinfoVo.setIndexMenuValue(it.getValue());
        });
        return userinfoVo;
    }

    /**
     * Menu tree the current user may see.
     *
     * @apiNote Platform and tenant sessions alike; a tenant session only sees the menus of its packages.
     * @param flush true rebuilds the menu cache from the database first
     * @return menu entries
     */
    @GetMapping("/menu")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    @SneakyThrows
    public List<EruptMenuVo> getMenu(@RequestParam(value = "flush", required = false, defaultValue = "false") boolean flush, HttpServletResponse response) {
        // flush=true rebuilds the current user's menu cache from the database,
        // ensuring the latest menu changes are served instead of the stale cached copy
        if (flush) eruptMenuService.flushMenuCache();
        return eruptTokenService.menuView(eruptContextService.getCurrentToken());
    }

    /**
     * Sign out and invalidate the token.
     *
     * @return ok
     */
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
     * Captcha image for login.
     *
     * @apiNote Returns a JPEG. The text lives 60 seconds under the given mark; send the same mark back as verifyCodeMark.
     * @param mark Client generated random number identifying this captcha
     * @param height Image height in pixels, clamped to 10..100
     */
    @GetMapping("/code-img")
    public void createCode(HttpServletResponse response, @RequestParam("mark") long mark,
                           @RequestParam(value = "height", required = false, defaultValue = "38") Integer height) throws Exception {
        // Clamp height to a sane captcha range to prevent resource abuse via oversized images
        height = Math.max(10, Math.min(height, 100));
        response.setContentType("image/jpeg"); // Set the response type format to image format
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache"); // Prohibit image caching
        response.setHeader("Cache-Control", "no-cache");
        Captcha captcha = new SpecCaptcha(150, height, 4);
        sessionService.put(SessionKey.VERIFY_CODE + mark, captcha.text(), 60, TimeUnit.SECONDS);
        captcha.out(response.getOutputStream());
    }


}
