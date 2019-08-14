package xyz.erupt.auth.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.auth.base.LoginModel;
import xyz.erupt.auth.constant.SessionKey;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.auth.model.EruptRole;
import xyz.erupt.auth.service.UserService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.bean.EruptApiModel;
import xyz.erupt.core.bean.TreeModel;
import xyz.erupt.core.session.SessionServiceImpl;
import xyz.erupt.core.util.DataHandlerUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by liyuepeng on 2018-12-13.
 */
@RestController
public class EruptUserController {

    @Autowired
    private Gson gson;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionServiceImpl sessionServiceImpl;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${erupt.expireTimeByLogin:60}")
    private Integer expireTimeByLogin;

    @PostMapping("/login")
    @ResponseBody
    @EruptRouter(loginVerify = false)
    public LoginModel login(@RequestParam("account") String account,
                            @RequestParam("pwd") String pwd,
                            @RequestParam(name = "verifyCode", required = false) String verifyCode,
                            HttpServletRequest request) {
        LoginModel loginModel = userService.login(account, pwd, verifyCode, request);
        if (loginModel.isPass()) {
            //生成token
            userService.createToken(loginModel);
            loginModel.setUserName(loginModel.getEruptUser().getName());

            List<EruptMenu> menuList;
            if (null != loginModel.getEruptUser().getIsAdmin() && loginModel.getEruptUser().getIsAdmin()) {
                menuList = entityManager.createQuery("from EruptMenu").getResultList();
            } else {
                Set<EruptMenu> menuSet = new HashSet<>();
                for (EruptRole role : loginModel.getEruptUser().getRoles()) {
                    if (role.getStatus()) {
                        menuSet.addAll(role.getMenus());
                    }
                }
                menuList = new ArrayList<>(menuSet);
                menuList.sort((m1, m2) -> {
                    Integer sort1 = m1.getSort();
                    Integer sort2 = m2.getSort();
                    if (null == sort1) {
                        sort1 = Integer.MAX_VALUE;
                    }
                    if (null == sort2) {
                        sort2 = Integer.MAX_VALUE;
                    }
                    return sort1.compareTo(sort2);
                });
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
            List<TreeModel> treeResultModels = DataHandlerUtil.treeModelToTree(treeModels);
            sessionServiceImpl.put(SessionKey.MENU_TREE + loginModel.getToken(), treeResultModels, expireTimeByLogin);
            sessionServiceImpl.put(SessionKey.MENU_LIST + loginModel.getToken(), menuList, expireTimeByLogin);
        }
        return loginModel;
    }

    @PostMapping("/change-pwd")
    @ResponseBody
    @EruptRouter
    public EruptApiModel changePwd(@RequestParam("account") String account,
                                   @RequestParam("pwd") String pwd,
                                   @RequestParam("newPwd") String newPwd,
                                   @RequestParam("newPwd2") String newPwd2) {
        return userService.changePwd(account, pwd, newPwd, newPwd2);
    }

    @GetMapping("/menu")
    @ResponseBody
    @EruptRouter
    public List<TreeModel> getMenu(HttpServletRequest request) {
        // type -> Set<EruptMenu>
        return sessionServiceImpl.get(SessionKey.MENU_TREE + request.getHeader("token"), new TypeToken<List<TreeModel>>() {
        }.getType());
    }
}
