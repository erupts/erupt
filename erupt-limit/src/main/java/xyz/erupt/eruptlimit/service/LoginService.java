package xyz.erupt.eruptlimit.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.model.TreeModel;
import xyz.erupt.core.util.MD5Utils;
import xyz.erupt.eruptcache.redis.RedisService;
import xyz.erupt.eruptlimit.base.LoginModel;
import xyz.erupt.eruptlimit.constant.RedisKey;
import xyz.erupt.eruptlimit.model.EruptMenu;
import xyz.erupt.eruptlimit.model.EruptUser;
import xyz.erupt.eruptlimit.repository.UserRepository;
import xyz.erupt.eruptlimit.util.DESUtil;
import xyz.erupt.eruptlimit.util.IpUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private Gson gson;


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
                String ipAddr = IpUtil.getIpAddr(request);
                for (String ip : eruptUser.getWhiteIp().split("\n")) {
                    if (ip.equals(ipAddr)) {
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
                loginErrorCount.put(account, 0);
                return new LoginModel(true, eruptUser);
            } else {
                Integer count = loginErrorCount.get(account);
                loginErrorCount.put(account, ++count);
                //错误五次要求使用验证码，错误50次锁定账号；
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


    public void createToken(LoginModel loginModel) {
        loginModel.setToken(DESUtil.encode(loginModel.getEruptUser().getAccount(), userTokenDES));
        redisService.put(RedisKey.USER_TOKEN + loginModel.getToken(), loginModel.getEruptUser().getId(), expireTimeByLogin);
    }

    public boolean verifyToken(String token) {
        if (null == redisService.get(RedisKey.USER_TOKEN + token)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean verifyMenuLimit(String token, String servletPath, EruptModel eruptModel) {
        //校验URL与请求头erupt信息是否匹配，来验证其合法性
        {
            boolean legalPath = false;
            for (String sp : servletPath.split("/")) {
                if (sp.equalsIgnoreCase(eruptModel.getEruptName())) {
                    legalPath = true;
                    break;
                }
            }
            if (!legalPath) {
                return false;
            }
        }
        {
            if (!eruptModel.getErupt().loginUse()) {
                return true;
            }
        }
        //校验菜单权限
        {
            List<EruptMenu> menus = gson.fromJson(redisService.get(RedisKey.MENU_LIST + token).toString(),
                    new TypeToken<List<EruptMenu>>() {
                    }.getType());
            boolean result = false;
            em:
            for (EruptMenu menu : menus) {
                if (StringUtils.isNotBlank(menu.getPath()) && menu.getPath().contains(eruptModel.getEruptName())) {
                    String[] pathArr = menu.getPath().split("/");
                    for (String pa : pathArr) {
                        if (pa.equalsIgnoreCase(eruptModel.getEruptName()) && !pa.startsWith(RestPath.NO_RIGHT_SYMBOL)) {
                            if (menu.getStatus() != 3) {
                                result = true;
                                break em;
                            }
                        }
                    }
                }
            }
            return result;
        }
    }

}
