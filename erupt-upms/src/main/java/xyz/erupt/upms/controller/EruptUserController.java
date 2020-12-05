package xyz.erupt.upms.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptMenuService;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.vo.EruptMenuVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2018-12-13.
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API)
public class EruptUserController {

    @Autowired
    private EruptMenuService menuService;

    @Autowired
    private EruptUserService eruptUserService;

    @Autowired
    private EruptSessionService sessionService;

    @Autowired
    private Gson gson;

    //登录
    @PostMapping("/login")
    @ResponseBody
    public LoginModel login(@RequestParam("account") String account,
                            @RequestParam("pwd") String pwd,
                            @RequestParam(name = "verifyCode", required = false) String verifyCode,
                            HttpServletRequest request) {
        LoginModel loginModel = eruptUserService.login(account, pwd, verifyCode, request);
        if (loginModel.isPass()) {
            //生成token
            EruptUser eruptUser = loginModel.getEruptUser();
            eruptUserService.createToken(loginModel);
            loginModel.setUserName(eruptUser.getName());
            EruptMenu indexMenu = eruptUser.getEruptMenu();
            if (null != indexMenu) {
                loginModel.setIndexMenu(indexMenu.getType() + "||" + indexMenu.getValue());
            }
            List<EruptMenu> eruptMenus = menuService.getMenuList(eruptUser);
            sessionService.put(SessionKey.MENU + loginModel.getToken(), gson.toJson(eruptMenus));
            sessionService.put(SessionKey.MENU_VIEW + loginModel.getToken(), gson.toJson(menuService.geneMenuListVo(eruptMenus)));
            //记录登录日志
            eruptUserService.saveLoginLog(eruptUser);
        }
        return loginModel;
    }

    //获取菜单
    @GetMapping("/menu")
    @ResponseBody
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, authIndex = 0)
    public List<EruptMenuVo> getMenu(HttpServletRequest request) {
        return sessionService.get(SessionKey.MENU_VIEW + request.getHeader("token"), new TypeToken<List<EruptMenuVo>>() {
        }.getType());
    }

    //登出
    @PostMapping("/logout")
    @ResponseBody
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, authIndex = 0)
    public EruptApiModel logout(HttpServletRequest request) {
        String token = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN);
        sessionService.remove(SessionKey.MENU + token);
        sessionService.remove(SessionKey.MENU_VIEW + token);
        sessionService.remove(SessionKey.USER_TOKEN + token);
        return EruptApiModel.successApi();
    }

    /**
     * 修改密码
     *
     * @param account 用户名
     * @param pwd     密码
     * @param newPwd  新密码
     * @param newPwd2 确认新密码
     * @return 结果
     */
    @PostMapping("/change-pwd")
    @ResponseBody
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, authIndex = 0)
    public EruptApiModel changePwd(@RequestParam("account") String account,
                                   @RequestParam("pwd") String pwd,
                                   @RequestParam("newPwd") String newPwd,
                                   @RequestParam("newPwd2") String newPwd2) {
        return eruptUserService.changePwd(account, pwd, newPwd, newPwd2);
    }


    /**
     * 生成验证码
     */
    @GetMapping
    @RequestMapping("/code-img")
    public void createCode(@RequestParam("account") String account, HttpServletResponse response) throws Exception {
        // 设置响应的类型格式为图片格式
        response.setContentType("image/jpeg");
        // 禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(150, 38, 3);
        // 验证码过期时间1分钟
        sessionService.put(SessionKey.VERIFY_CODE + account, captcha.text(), 60);
        // 响应图片
        captcha.out(response.getOutputStream());
    }

}
