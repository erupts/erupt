package xyz.erupt.eruptlimit.controller;

import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.base.model.EruptModel;
import xyz.erupt.base.model.TreeModel;
import xyz.erupt.eruptlimit.base.LoginModel;
import xyz.erupt.eruptlimit.constant.RedisKey;
import xyz.erupt.eruptlimit.model.EruptMenu;
import xyz.erupt.eruptlimit.model.EruptRole;
import xyz.erupt.eruptlimit.service.LoginService;
import xyz.erupt.service.CoreService;
import xyz.erupt.util.EruptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by liyuepeng on 2018-12-13.
 */
@RestController
@RequestMapping("erupt-user")
public class EruptUserController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    @ResponseBody
    public LoginModel login(@RequestParam("account") String account,
                            @RequestParam("pwd") String pwd,
                            @RequestParam(name = "verifyCode", required = false) String verifyCode,
                            HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        LoginModel loginModel = loginService.login(account, pwd, verifyCode, request.getSession());
        if (loginModel.isPass()) {
            //生成token
            loginService.createToken(loginModel);
            loginModel.setUserName(loginModel.getEruptUser().getName());
//            Set<EruptMenu> menuSet = new HashSet<>();
//            for (EruptRole role : loginModel.getEruptUser().getRoles()) {
//                menuSet.addAll(role.getMenus());
//            }
//            //生成tree结构数据
//            EruptModel eruptModel = CoreService.ERUPTS.get(EruptMenu.class.getSimpleName());
//            Tree tree = eruptModel.getErupt().tree();
//            List<TreeModel> treeModels = new ArrayList<>();
//            for (Object o : menuSet) {
//                Map<String, Object> map = (Map) o;
//                TreeModel treeModel = new TreeModel(map.get(tree.id()), map.get(tree.label()), map.get(tree.pid().replace(".", "_")), o);
//                treeModels.add(treeModel);
//            }
//            List<TreeModel> treeResultModels = EruptUtil.TreeModelToTree(treeModels);
//            request.getSession().setAttribute(RedisKey.MENU, treeResultModels);
        }
        return loginModel;
    }

    @PostMapping("/menu")
    @ResponseBody
    public Object getMenu(HttpServletRequest request) {
        //type -> Set<EruptMenu>
        return request.getSession().getAttribute(RedisKey.MENU);
    }
}
