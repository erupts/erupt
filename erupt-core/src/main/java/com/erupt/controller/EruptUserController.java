package com.erupt.controller;

import com.erupt.annotation.sub_erupt.Tree;
import com.erupt.base.model.EruptModel;
import com.erupt.base.model.TreeModel;
import com.erupt.constant.RestPath;
import com.erupt.constant.SessionKey;
import com.erupt.model.EruptMenu;
import com.erupt.model.EruptRole;
import com.erupt.base.model.LoginModel;
import com.erupt.service.CoreService;
import com.erupt.service.LoginService;
import com.erupt.util.EruptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by liyuepeng on 2018-12-13.
 */
@RestController
@RequestMapping(RestPath.ERUPT_USER)
public class EruptUserController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    @ResponseBody
    public LoginModel getEruptTableView(@RequestParam("account") String account,
                                        @RequestParam("pwd") String pwd,
                                        @RequestParam(name = "verifyCode", required = false) String verifyCode,
                                        HttpServletRequest request) {
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
