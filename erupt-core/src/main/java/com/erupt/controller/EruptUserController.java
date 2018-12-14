package com.erupt.controller;

import com.erupt.constant.RestPath;
import com.erupt.model.EruptApiModel;
import com.erupt.model.LoginModel;
import com.erupt.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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
        LoginModel loginModel = loginService.login(account,pwd,verifyCode,request.getSession());
        //加载菜单
        if (loginModel.isPass()){

        }
        return loginModel;
    }
}
