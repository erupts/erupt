package com.erupt.service;

import com.erupt.constant.SessionKey;
import com.erupt.model.EruptUser;
import com.erupt.base.model.LoginModel;
import com.erupt.repository.UserRepository;
import com.erupt.util.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liyuepeng on 2018-12-13.
 */
@Service
public class LoginService {

    @Autowired
    private UserRepository userRepository;


    //TODO 分布式下计数会不准
    private Map<String, Integer> loginErrorCount = new HashMap<>();


    public LoginModel login(String account, String pwd, String verifyCode, HttpSession session) {
        if (loginErrorCount.get(account) >= 5) {
            if (StringUtils.isBlank(verifyCode)) {
                return new LoginModel(false, "请填写验证码");
            }
            session.getAttribute(SessionKey.VERIFY_CODE);
            String oldStr = session.getAttribute(SessionKey.VERIFY_CODE).toString();
            if (!oldStr.equalsIgnoreCase(verifyCode)) {
                return new LoginModel(false, "验证码不正确");
            }
        }
        EruptUser eruptUser = userRepository.findByAccount(account);
        boolean pass = false;
        if (null != eruptUser) {
            if (!eruptUser.getStatus()) {
                return new LoginModel(false, "账号已锁定");
            }
            if (eruptUser.getIsMD5()) {
                if (MD5Utils.digest(pwd).equals(pwd)) {
                    pass = true;
                }
            } else {
                if (eruptUser.getPassword().equals(pwd)) {
                    pass = true;
                }
            }
            if (pass) {
                return new LoginModel(true, eruptUser);
            } else {
                Integer count = loginErrorCount.get(account);
                loginErrorCount.put(account, ++count);
                //错误五次则开启验证码模式模式，错误50次锁定账号；
                if (loginErrorCount.get(account) >= 5) {
                    return new LoginModel(false, "密码错误", true);
                } else if (loginErrorCount.get(account) >= 50) {
                    eruptUser.setStatus(false);
                    return new LoginModel(false, "账号被锁定");
                } else {
                    return new LoginModel(false, "密码错误");
                }
            }
        } else {
            return new LoginModel(false, "用户不存在");
        }
    }


}
