package xyz.erupt.upms.controller;

import com.google.gson.reflect.TypeToken;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.util.SecretUtil;
import xyz.erupt.core.view.EruptApiModel;
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

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
     * 登录
     *
     * @param account        用户名
     * @param pwd            密码
     * @param verifyCode     验证码
     * @param verifyCodeMark 验证码标识
     */
    @SneakyThrows
    @GetMapping(value = "/login")
    public LoginModel login(@RequestParam String account, @RequestParam String pwd,
                            @RequestParam(required = false) String verifyCode,
                            @RequestParam(required = false) String verifyCodeMark
    ) {
        if (!eruptUserService.checkVerifyCode(account, verifyCode, verifyCodeMark)) {
            return new LoginModel(false, "验证码错误", true);
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
            eruptUserService.saveLoginLog(eruptUser, loginModel.getToken()); //记录登录日志
        }
        return loginModel;
    }


    /**
     * 修改密码
     *
     * @param pwd     旧密码
     * @param newPwd  新密码
     * @param newPwd2 确认新密码
     */
    @GetMapping(value = "/change-pwd")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel changePwd(@RequestParam("pwd") String pwd, @RequestParam("newPwd") String newPwd, @RequestParam("newPwd2") String newPwd2) {
        pwd = SecretUtil.decodeSecret(pwd, 3);
        newPwd = SecretUtil.decodeSecret(newPwd, 3);
        newPwd2 = SecretUtil.decodeSecret(newPwd2, 3);
        return eruptUserService.changePwd(eruptUserService.getCurrentAccount(), pwd, newPwd, newPwd2);
    }

    //用户信息
    @GetMapping("/userinfo")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptUserinfoVo userinfo() {
        EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
        EruptUserinfoVo userinfoVo = new EruptUserinfoVo();
        userinfoVo.setNickname(eruptUser.getName());
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

    //获取菜单列表
    @GetMapping("/menu")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public List<EruptMenuVo> getMenu() {
        List<EruptMenuVo> menus = sessionService.get(SessionKey.MENU_VIEW + eruptContextService.getCurrentToken(), new TypeToken<List<EruptMenuVo>>() {
        }.getType());
        menus.forEach(it -> it.setName(I18nTranslate.$translate(it.getName())));
        return menus;
    }

    //登出
    @GetMapping(value = "/logout")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel logout() {
        String token = eruptContextService.getCurrentToken();
        MetaUserinfo metaUserinfo = eruptUserService.getSimpleUserInfo();
        LoginProxy loginProxy = EruptUserService.findEruptLogin();
        Optional.ofNullable(loginProxy).ifPresent(it -> it.logout(token));
        eruptTokenService.logoutToken(metaUserinfo.getUsername(), token);
        return EruptApiModel.successApi();
    }

    /**
     * 生成验证码
     *
     * @param mark   生成验证码标记值
     * @param height 验证码高度
     */
    @GetMapping("/code-img")
    public void createCode(HttpServletResponse response, @RequestParam long mark,
                           @RequestParam(required = false, defaultValue = "38") Integer height) throws Exception {
        response.setContentType("image/jpeg"); // 设置响应的类型格式为图片格式
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache"); // 禁止图像缓存
        response.setHeader("Cache-Control", "no-cache");
        Captcha captcha = new SpecCaptcha(150, height, 4);
        sessionService.put(SessionKey.VERIFY_CODE + mark, captcha.text(), 60, TimeUnit.SECONDS);
        captcha.out(response.getOutputStream());
    }


}
