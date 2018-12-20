package com.erupt.eruptlimit.controller;

import com.erupt.annotation.sub_erupt.Tree;
import com.erupt.base.model.EruptModel;
import com.erupt.base.model.TreeModel;
import com.erupt.eruptlimit.base.LoginModel;
import com.erupt.eruptlimit.constant.SessionKey;
import com.erupt.eruptlimit.model.EruptMenu;
import com.erupt.eruptlimit.model.EruptRole;
import com.erupt.eruptlimit.service.LoginService;
import com.erupt.service.CoreService;
import com.erupt.util.EruptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        request.getSession().setAttribute("abc", 123123);
        LoginModel loginModel = loginService.login(account, pwd, verifyCode, request.getSession());
        if (loginModel.isPass()) {
            request.getSession().setAttribute(SessionKey.IS_LOGIN, true);
            Set<EruptMenu> menuSet = new HashSet<>();
            for (EruptRole role : loginModel.getEruptUser().getRoles()) {
                menuSet.addAll(role.getMenus());
            }
            //生成tree结构数据
            EruptModel eruptModel = CoreService.ERUPTS.get(EruptMenu.class.getSimpleName());
            Tree tree = eruptModel.getErupt().tree();
            List<TreeModel> treeModels = new ArrayList<>();
            for (Object o : menuSet) {
                Map<String, Object> map = (Map) o;
                TreeModel treeModel = new TreeModel(map.get(tree.id()), map.get(tree.label()), map.get(tree.pid().replace(".", "_")), o);
                treeModels.add(treeModel);
            }
            List<TreeModel> treeResultModels = EruptUtil.TreeModelToTree(treeModels);
            request.getSession().setAttribute(SessionKey.MENU, treeResultModels);
        }
        return loginModel;
    }

    @PostMapping("/menu")
    @ResponseBody
    public Object getMenu(HttpServletRequest request) {
        //type -> Set<EruptMenu>
        return request.getSession().getAttribute(SessionKey.MENU);
    }
}
