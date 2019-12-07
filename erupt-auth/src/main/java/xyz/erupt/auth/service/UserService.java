package xyz.erupt.auth.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.auth.base.LoginModel;
import xyz.erupt.auth.config.EruptAuthConfig;
import xyz.erupt.auth.constant.SessionKey;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.auth.model.EruptUserLoginLog;
import xyz.erupt.auth.repository.UserRepository;
import xyz.erupt.auth.util.IpUtil;
import xyz.erupt.auth.util.MD5Utils;
import xyz.erupt.core.bean.EruptApiModel;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.service.SessionService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liyuepeng on 2018-12-13.
 */
@Service
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private EruptAuthConfig eruptAuthConfig;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private Gson gson;


    //TODO 分布式下计数会不准
    private static Map<String, Integer> loginErrorCount = new HashMap<>();

    @Transactional
    public void saveLoginLog(EruptUser user) {
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        EruptUserLoginLog loginLog = new EruptUserLoginLog();
        loginLog.setEruptUser(user);
        loginLog.setLoginTime(new Date());
        loginLog.setIp(IpUtil.getIpAddr(request));
        loginLog.setSystem(userAgent.getOperatingSystem().getName());
        loginLog.setBrowser(userAgent.getBrowser().getName() + " "
                + (userAgent.getBrowserVersion() == null ? "" : userAgent.getBrowserVersion().getMajorVersion()));
        loginLog.setDeviceType(userAgent.getOperatingSystem().getDeviceType().getName());
        entityManager.persist(loginLog);
    }


    public LoginModel login(String account, String pwd, String verifyCode, HttpServletRequest request) {
        loginErrorCount.putIfAbsent(account, 0);
        if (loginErrorCount.get(account) >= 3) {
            if (StringUtils.isBlank(verifyCode)) {
                return new LoginModel(false, "请填写验证码", true);
            }
            Object vc = sessionService.get(SessionKey.VERIFY_CODE + account);
            sessionService.remove(SessionKey.VERIFY_CODE + account);
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
            if (eruptUser.getIsMd5()) {
                if (MD5Utils.digest(pwd).equalsIgnoreCase(eruptUser.getPassword())) {
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

    @Transactional
    public EruptApiModel changePwd(String account, String pwd, String newPwd, String newPwd2) {
        if (!newPwd.equals(newPwd2)) {
            return EruptApiModel.errorNoInterceptApi("修改失败，新密码与确认密码不匹配");
        }
        EruptUser eruptUser = userRepository.findByAccount(account);
        if (eruptUser.getIsMd5()) {
            pwd = MD5Utils.digest(pwd);
            newPwd = MD5Utils.digest(newPwd);
        }
        if (eruptUser.getPassword().equals(pwd)) {
            if (newPwd.equals(eruptUser.getPassword())) {
                return EruptApiModel.errorNoInterceptApi("修改失败，新密码不能和原始密码一样");
            }
            eruptUser.setPassword(newPwd);
            entityManager.merge(eruptUser);
            return EruptApiModel.successApi();
        } else {
            return EruptApiModel.errorNoInterceptApi("密码错误");
        }
    }


    public void createToken(LoginModel loginModel) {
        loginModel.setToken(RandomStringUtils.random(20, true, true));
//        loginModel.getEruptUser().setRoles(null);
        sessionService.put(SessionKey.USER_TOKEN + loginModel.getToken(), loginModel.getEruptUser().getId(), eruptAuthConfig.getExpireTimeByLogin());
    }

    public Long getCurrUserId() {
        String token = request.getHeader("token");
        return (Long) sessionService.get(SessionKey.USER_TOKEN + token);
    }

    public boolean verifyToken(String token) {
        if (null == sessionService.get(SessionKey.USER_TOKEN + token)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean verifyMenuLimit(String token, String authStr, EruptModel eruptModel) {
        //校验authStr与请求头erupt信息是否匹配，来验证其合法性
        {
            if (!authStr.equalsIgnoreCase(eruptModel.getEruptName())) {
                return false;
            }
        }
        //检验注解
        {
            if (!eruptModel.getErupt().loginUse()) {
                return true;
            }
        }
        //校验菜单权限
        {
            List<EruptMenu> menus = sessionService.get(SessionKey.MENU_LIST + token, new TypeToken<List<EruptMenu>>() {
            }.getType());
            boolean result = false;
            em:
            for (EruptMenu menu : menus) {
                if (StringUtils.isNotBlank(menu.getPath()) && menu.getPath().toLowerCase().contains(eruptModel.getEruptName().toLowerCase())) {
                    String[] pathArr = menu.getPath().split("\\?")[0].split("/");
                    for (String pa : pathArr) {
                        if (pa.equalsIgnoreCase(eruptModel.getEruptName())) {
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
