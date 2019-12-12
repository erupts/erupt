package xyz.erupt.auth.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.auth.base.LoginModel;
import xyz.erupt.auth.config.EruptAuthConfig;
import xyz.erupt.auth.constant.SessionKey;
import xyz.erupt.auth.interceptor.LoginInterceptor;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.auth.model.EruptRole;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.auth.service.UserService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.bean.EruptApiModel;
import xyz.erupt.core.bean.TreeModel;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.service.SessionService;
import xyz.erupt.core.util.DataHandlerUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by liyuepeng on 2018-12-13.
 */
@RestController
@RequestMapping(RestPath.ERUPT_API)
public class EruptUserController {

    @Autowired
    private Gson gson;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EruptAuthConfig eruptAuthConfig;

    @PostMapping("/login")
    @ResponseBody
    public LoginModel login(@RequestParam("account") String account,
                            @RequestParam("pwd") String pwd,
                            @RequestParam(name = "verifyCode", required = false) String verifyCode,
                            HttpServletRequest request) {
        LoginModel loginModel = userService.login(account, pwd, verifyCode, request);
        if (loginModel.isPass()) {
            //生成token
            EruptUser eruptUser = loginModel.getEruptUser();
            userService.createToken(loginModel);
            loginModel.setUserName(eruptUser.getName());
            if (null != eruptUser.getEruptMenu()) {
                loginModel.setIndexPath(eruptUser.getEruptMenu().getPath());
            }
            List<EruptMenu> menuList;
            if (null != eruptUser.getIsAdmin() && eruptUser.getIsAdmin()) {
                menuList = entityManager.createQuery("from EruptMenu order by sort").getResultList();
            } else {
                Set<EruptMenu> menuSet = new HashSet<>();
                for (EruptRole role : eruptUser.getRoles()) {
                    if (role.getStatus()) {
                        menuSet.addAll(role.getMenus());
                    }
                }
                menuList = menuSet.stream().sorted(Comparator.comparing(EruptMenu::getSort)).collect(Collectors.toList());
            }
            //生成tree结构数据
            List<TreeModel> treeModels = new ArrayList<>();
            for (EruptMenu eruptMenu : menuList) {
                Long pid = null;
                if (null != eruptMenu.getParentMenu()) {
                    pid = eruptMenu.getParentMenu().getId();
                }
                TreeModel treeModel = new TreeModel(eruptMenu.getId(), eruptMenu.getName(), pid, eruptMenu);
                treeModels.add(treeModel);
            }
            List<TreeModel> treeResultModels = DataHandlerUtil.treeModelToTree(treeModels, null);
            sessionService.put(SessionKey.MENU_TREE + loginModel.getToken(), treeResultModels, eruptAuthConfig.getExpireTimeByLogin());
            sessionService.put(SessionKey.MENU_LIST + loginModel.getToken(), menuList, eruptAuthConfig.getExpireTimeByLogin());
            //记录登录日志
            userService.saveLoginLog(eruptUser);
        }
        return loginModel;
    }

    @PostMapping("/logout")
    @ResponseBody
    @EruptRouter(verifyErupt = false)
    public EruptApiModel logout(HttpServletRequest request) {
        String token = request.getHeader(LoginInterceptor.ERUPT_HEADER_TOKEN);
        sessionService.remove(SessionKey.MENU_LIST + token);
        sessionService.remove(SessionKey.MENU_TREE + token);
        sessionService.remove(SessionKey.USER_TOKEN + token);
        return EruptApiModel.successApi();
    }


    @PostMapping("/change-pwd")
    @ResponseBody
    @EruptRouter(verifyErupt = false)
    public EruptApiModel changePwd(@RequestParam("account") String account,
                                   @RequestParam("pwd") String pwd,
                                   @RequestParam("newPwd") String newPwd,
                                   @RequestParam("newPwd2") String newPwd2) {
        return userService.changePwd(account, pwd, newPwd, newPwd2);
    }

    @GetMapping("/menu")
    @ResponseBody
    @EruptRouter(verifyErupt = false)
    public List<TreeModel> getMenu(HttpServletRequest request) {
        // type -> Set<EruptMenu>
        return sessionService.get(SessionKey.MENU_TREE + request.getHeader("token"), new TypeToken<List<TreeModel>>() {
        }.getType());
    }

}
