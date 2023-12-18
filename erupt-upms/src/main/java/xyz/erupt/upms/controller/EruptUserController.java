package xyz.erupt.upms.controller;

import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.web.prop.EruptAppProp;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.util.SecretUtil;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private EruptContextService eruptContextService;

    @Resource
    private HttpServletRequest request;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

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
