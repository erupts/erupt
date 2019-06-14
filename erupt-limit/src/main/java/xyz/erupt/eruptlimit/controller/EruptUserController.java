package xyz.erupt.eruptlimit.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.cache.EruptRedisService;
import xyz.erupt.core.model.TreeModel;
import xyz.erupt.core.util.DataHandlerUtil;
import xyz.erupt.eruptlimit.base.LoginModel;
import xyz.erupt.eruptlimit.constant.RedisKey;
import xyz.erupt.eruptlimit.model.EruptMenu;
import xyz.erupt.eruptlimit.model.EruptRole;
import xyz.erupt.eruptlimit.service.LoginService;

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
    private LoginService loginService;

    @Autowired
    private EruptRedisService redisService;

    @Value("${erupt.expireTimeByLogin:60}")
    private Integer expireTimeByLogin;

    @Autowired
    private Gson gson;

    @PostMapping("/login")
    @ResponseBody
    @EruptRouter(loginVerify = false)
    public LoginModel login(@RequestParam("account") String account,
                            @RequestParam("pwd") String pwd,
                            @RequestParam(name = "verifyCode", required = false) String verifyCode,
                            HttpServletRequest request) {
        LoginModel loginModel = loginService.login(account, pwd, verifyCode, request);
        if (loginModel.isPass()) {
            //生成token
            loginService.createToken(loginModel);
            loginModel.setUserName(loginModel.getEruptUser().getName());

            Set<EruptMenu> menuSet = new HashSet<>();
            for (EruptRole role : loginModel.getEruptUser().getRoles()) {
                if (role.getStatus()) {
                    menuSet.addAll(role.getMenus());
                }
            }
            List<EruptMenu> menuList = new ArrayList<>(menuSet);
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
            redisService.put(RedisKey.MENU_TREE + loginModel.getToken(), treeResultModels, expireTimeByLogin);
            redisService.put(RedisKey.MENU_LIST + loginModel.getToken(), menuList, expireTimeByLogin);
        }
        return loginModel;
    }

    @GetMapping("/menu")
    @ResponseBody
    @EruptRouter
    public List<TreeModel> getMenu(HttpServletRequest request) {
//        type -> Set<EruptMenu>
        Object o = redisService.get(RedisKey.MENU_TREE + request.getHeader("token"));
        return gson.fromJson(o.toString(), new TypeToken<List<TreeModel>>() {
        }.getType());
    }
}
