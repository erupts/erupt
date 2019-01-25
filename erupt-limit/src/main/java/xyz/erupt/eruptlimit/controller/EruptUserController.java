package xyz.erupt.eruptlimit.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.dao.EruptJpaDao;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.model.Page;
import xyz.erupt.core.model.TreeModel;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.eruptcache.redis.RedisService;
import xyz.erupt.eruptlimit.base.LoginModel;
import xyz.erupt.eruptlimit.constant.RedisKey;
import xyz.erupt.eruptlimit.model.EruptMenu;
import xyz.erupt.eruptlimit.model.EruptRole;
import xyz.erupt.eruptlimit.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by liyuepeng on 2018-12-13.
 */
@RestController
public class EruptUserController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private EruptJpaDao eruptJpaDao;

    @Autowired
    private RedisService redisService;

    @PostMapping(RestPath.DONT_INTERCEPT + "/login")
    @ResponseBody
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
                menuSet.addAll(role.getMenus());
            }
            //生成tree结构数据
            EruptModel eruptModel = CoreService.ERUPTS.get(EruptMenu.class.getSimpleName());
            Tree tree = eruptModel.getErupt().tree();
            List<TreeModel> treeModels = new ArrayList<>();
            for (EruptMenu eruptMenu : menuSet) {
                Long pid = null;
                if (null != eruptMenu.getParentMenu()) {
                    pid = eruptMenu.getParentMenu().getId();
                }
                TreeModel treeModel = new TreeModel(eruptMenu.getId(), eruptMenu.getName(), pid, eruptMenu);
                treeModels.add(treeModel);
            }
            List<TreeModel> treeResultModels = EruptUtil.TreeModelToTree(treeModels);
            redisService.put(RedisKey.MENU + loginModel.getToken(), treeResultModels);
        }
        return loginModel;
    }

    @GetMapping("/menu")
    @ResponseBody
    public Object getMenu(HttpServletRequest request) {
//        type -> Set<EruptMenu>
        EruptModel eruptModel = CoreService.ERUPTS.get("EruptMenu");
        Object o = redisService.get(RedisKey.MENU + request.getHeader("token"));
        return new Gson().fromJson(o.toString(),new TypeToken<List<TreeModel>>(){}.getType() );
    }
}
