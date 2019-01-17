package xyz.erupt.eruptlimit.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.erupt.core.util.MD5Utils;
import xyz.erupt.eruptcache.redis.RedisService;
import xyz.erupt.eruptlimit.base.LoginModel;
import xyz.erupt.eruptlimit.constant.LimitConst;
import xyz.erupt.eruptlimit.constant.RedisKey;
import xyz.erupt.eruptlimit.model.EruptUser;
import xyz.erupt.eruptlimit.repository.UserRepository;
import xyz.erupt.eruptlimit.util.DESUtil;
import xyz.erupt.eruptlimit.util.IpUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liyuepeng on 2018-12-13.
 */
@Service
public class LoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisService redisService;

    @Value("${erupt.expireTimeByLogin}")
    private Integer expireTimeByLogin;

    //每次服务器启动后内存中的tokenDES都会不一样，就连开发人员都查不到，杜绝了token被仿造的问题
    private static final String userTokenDES = RandomStringUtils.randomGraph(10);


    //TODO 分布式下计数会不准
    private static Map<String, Integer> loginErrorCount = new HashMap<>();


    public LoginModel login(String account, String pwd, String verifyCode, HttpServletRequest request) {
        loginErrorCount.putIfAbsent(account, 0);
        if (loginErrorCount.get(account) >= 5) {
            if (StringUtils.isBlank(verifyCode)) {
                return new LoginModel(false, "请填写验证码", true);
            }
            Object vc = redisService.getStr(RedisKey.VERIFY_CODE + account);
            if (vc == null || !vc.toString().equalsIgnoreCase(verifyCode)) {
                return new LoginModel(false, "验证码不正确", true);
            }
        }
        EruptUser eruptUser = userRepository.findByAccount(account);
        if (null != eruptUser) {
            if (!eruptUser.getStatus()) {
                return new LoginModel(false, "账号已锁定!");
            }
            //校验IP
            if (StringUtils.isNotBlank(eruptUser.getWhiteIp())) {
                boolean isAllowIp = false;
                for (String s : eruptUser.getWhiteIp().split("\n")) {
                    if (s.equals(IpUtil.getIpAddr(request))) {
                        isAllowIp = true;
                        break;
                    }
                }
                if (!isAllowIp) {
                    return new LoginModel(false, "ip不允许访问");
                }
            }
            //校验密码
            boolean pass = false;
            if (eruptUser.getIsMD5()) {
                if (MD5Utils.digest(pwd).equals(eruptUser.getPassword())) {
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
                    return new LoginModel(false, "密码错误 -");
                }
            }
        } else {
            return new LoginModel(false, "用户不存在");
        }
    }


    public void createToken(LoginModel loginModel) {
        loginModel.setToken(DESUtil.encode(loginModel.getEruptUser().getAccount(), userTokenDES));
        redisService.put(RedisKey.USER_TOKEN + loginModel.getToken(), true, expireTimeByLogin);
    }

    public boolean verifyToken(String token) {
        if (null == redisService.get(RedisKey.USER_TOKEN + token)) {
            return false;
        } else {
            return true;
        }
    }

    public static void main(String[] args) {
        System.out.println(LoginService.userTokenDES);
    }

}
