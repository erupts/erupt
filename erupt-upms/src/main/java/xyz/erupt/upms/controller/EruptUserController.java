package xyz.erupt.upms.controller;

import com.google.gson.reflect.TypeToken;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.EruptAppProp;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.config.EruptUpmsConfig;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.fun.LoginProxy;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.util.IpUtil;
import xyz.erupt.upms.vo.EruptMenuVo;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private EruptUpmsConfig eruptUpmsConfig;

    //登录
    @SneakyThrows
    @PostMapping("/login")
    public LoginModel login(@RequestParam("account") String account,
                            @RequestParam("pwd") String pwd,
                            @RequestParam(name = "verifyCode", required = false) String verifyCode) {
        if (!eruptUserService.checkVerifyCode(verifyCode)) {
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
                loginModel.setEruptUser(eruptUser);
                loginModel.setPass(true);
            } catch (Exception e) {
                if (0 == eruptAppProp.getVerifyCodeCount()) {
                    loginModel.setUseVerifyCode(true);
                }
                loginModel.setReason(e.getMessage());
                loginModel.setPass(false);

            }
        }
        if (loginModel.isPass()) {
            EruptUser eruptUser = loginModel.getEruptUser();
            loginModel.setToken(RandomStringUtils.random(16, true, true));
            loginModel.setExpire(this.eruptUserService.getExpireTime());
            sessionService.put(SessionKey.USER_TOKEN + loginModel.getToken(), loginModel.getEruptUser().getId().toString(), eruptUpmsConfig.getExpireTimeByLogin());
            if (null != loginProxy) {
                loginProxy.loginSuccess(eruptUser, loginModel.getToken());
            }
            eruptUserService.cacheUserInfo(eruptUser, loginModel.getToken());
            eruptUserService.saveLoginLog(eruptUser, loginModel.getToken()); //记录登录日志
        }
        return loginModel;
    }

    //获取菜单
    @GetMapping("/menu")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, authIndex = 0)
    public List<EruptMenuVo> getMenu() {
        return sessionService.get(SessionKey.MENU_VIEW + eruptUserService.getCurrentToken(), new TypeToken<List<EruptMenuVo>>() {
        }.getType());
    }

    //登出
    @PostMapping("/logout")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, authIndex = 0)
    public EruptApiModel logout(HttpServletRequest request) {
        String token = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN);
        LoginProxy loginProxy = EruptUserService.findEruptLogin();
        if (null != loginProxy) {
            loginProxy.logout(token);
        }
        sessionService.remove(SessionKey.MENU_VALUE_MAP + token);
        sessionService.remove(SessionKey.MENU_CODE_MAP + token);
        sessionService.remove(SessionKey.MENU_VIEW + token);
        sessionService.remove(SessionKey.USER_TOKEN + token);
        return EruptApiModel.successApi();
    }

    // 修改密码
    @PostMapping("/change-pwd")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, authIndex = 0)
    public EruptApiModel changePwd(@RequestParam("account") String account,
                                   @RequestParam("pwd") String pwd,
                                   @RequestParam("newPwd") String newPwd,
                                   @RequestParam("newPwd2") String newPwd2) {
        return eruptUserService.changePwd(account, pwd, newPwd, newPwd2);
    }

    @GetMapping("/token-valid")
    public boolean tokenValid() {
        return sessionService.get(eruptUserService.getCurrentToken()) != null;
    }

    // 生成验证码
    @GetMapping
    @RequestMapping("/code-img")
    public void createCode(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("image/jpeg"); // 设置响应的类型格式为图片格式
        response.setHeader("Pragma", "no-cache"); // 禁止图像缓存。
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        Captcha captcha = new SpecCaptcha(150, 38, 4);
        sessionService.put(SessionKey.VERIFY_CODE + IpUtil.getIpAddr(request), captcha.text(), 60, TimeUnit.SECONDS);
        captcha.out(response.getOutputStream()); // 响应图片
    }

}
