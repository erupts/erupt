package xyz.erupt.auth.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wf.captcha.ArithmeticCaptcha;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.auth.base.LoginModel;
import xyz.erupt.auth.constant.SessionKey;
import xyz.erupt.auth.interceptor.LoginInterceptor;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.auth.service.EruptMenuService;
import xyz.erupt.auth.service.EruptSessionService;
import xyz.erupt.auth.service.EruptUserService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.TreeModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liyuepeng
 * @date 2018-12-13.
 */
@RestController
@RequestMapping(RestPath.ERUPT_API)
public class EruptUserController {

    @Autowired
    private EruptMenuService menuService;

    @Autowired
    private EruptUserService eruptUserService;

    @Autowired
    private EruptSessionService sessionService;

    @Autowired
    private Gson gson;

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
            if (null != eruptUser.getEruptMenu()) {
                loginModel.setIndexPath(eruptUser.getEruptMenu().getPath());
            }
            List<EruptMenu> menuList = menuService.geneMenuList(loginModel.getEruptUser());
            List<TreeModel> treeResultModels = menuService.geneMenuTree(menuList);
            sessionService.put(SessionKey.MENU_TREE + loginModel.getToken(), gson.toJson(treeResultModels));
            sessionService.put(SessionKey.MENU_LIST + loginModel.getToken(), gson.toJson(menuList));
            //记录登录日志
            eruptUserService.saveLoginLog(eruptUser);
        }
        return loginModel;
    }

    @PostMapping("/logout")
    @ResponseBody
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, authIndex = -1)
    public EruptApiModel logout(HttpServletRequest request) {
        String token = request.getHeader(LoginInterceptor.ERUPT_HEADER_TOKEN);
        sessionService.remove(SessionKey.MENU_LIST + token);
        sessionService.remove(SessionKey.MENU_TREE + token);
        sessionService.remove(SessionKey.USER_TOKEN + token);
        return EruptApiModel.successApi();
    }

    @PostMapping("/change-pwd")
    @ResponseBody
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, authIndex = -1)
    public EruptApiModel changePwd(@RequestParam("account") String account,
                                   @RequestParam("pwd") String pwd,
                                   @RequestParam("newPwd") String newPwd,
                                   @RequestParam("newPwd2") String newPwd2) {
        return eruptUserService.changePwd(account, pwd, newPwd, newPwd2);
    }

    @GetMapping("/menu")
    @ResponseBody
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, authIndex = -1)
    public List<TreeModel> getMenu(HttpServletRequest request) {
        // type -> Set<EruptMenu>
        return sessionService.get(SessionKey.MENU_TREE + request.getHeader("token"), new TypeToken<List<TreeModel>>() {
        }.getType());
    }

    @GetMapping("/menu-list")
    @ResponseBody
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, authIndex = -1)
    public List<EruptMenu> getMenuList(HttpServletRequest request) {
        List<EruptMenu> eruptMenu = sessionService.get(SessionKey.MENU_LIST + request.getHeader("token"), new TypeToken<List<EruptMenu>>() {
        }.getType());
        return eruptMenu.stream()
                .filter(em -> StringUtils.isNotBlank(em.getPath()))
                .filter(em -> em.getStatus() == 1)
                .peek(em -> {
                    em.setParam(null);
                    em.setParentMenu(null);
                    em.setId(null);
                    em.setStatus(null);
                }).collect(Collectors.toList());
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
