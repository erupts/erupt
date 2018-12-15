package com.erupt.controller;

import com.erupt.constant.RestPath;
import com.erupt.constant.SessionKey;
import com.erupt.model.EruptMenu;
import com.erupt.model.EruptRole;
import com.erupt.base.model.LoginModel;
import com.erupt.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

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
            Set<EruptMenu> menus = new HashSet<>();
            for (EruptRole role : loginModel.getEruptUser().getRoles()) {
                menus.addAll(role.getMenus());
            }
            request.getSession().setAttribute(SessionKey.MENU, menus);
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
