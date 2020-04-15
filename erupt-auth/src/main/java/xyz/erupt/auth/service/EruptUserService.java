package xyz.erupt.auth.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.auth.base.LoginModel;
import xyz.erupt.auth.config.EruptAuthConfig;
import xyz.erupt.auth.constant.SessionKey;
import xyz.erupt.auth.interceptor.LoginInterceptor;
import xyz.erupt.auth.model.EruptLoginLog;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.auth.repository.UserRepository;
import xyz.erupt.auth.util.IpUtil;
import xyz.erupt.auth.util.MD5Utils;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptModel;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.*;

/**
 * @author liyuepeng
 * @date 2018-12-13.
 */
@Service
public class EruptUserService {

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

    public static final String USER_AGENT = "User-Agent";


    //TODO 分布式下计数会不准
    private static Map<String, Integer> loginErrorCount = new HashMap<>();

    @Transactional
    public void saveLoginLog(EruptUser user) {
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        EruptLoginLog loginLog = new EruptLoginLog();
        loginLog.setEruptUser(user);
        loginLog.setLoginTime(new Date());
        loginLog.setIp(IpUtil.getIpAddr(request));
        loginLog.setSystemName(userAgent.getOperatingSystem().getName());
        loginLog.setBrowser(userAgent.getBrowser().getName());
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
            {
                String digestPwd;
                if (eruptUser.getIsMd5()) {
                    digestPwd = eruptUser.getPassword();
                } else {
                    digestPwd = MD5Utils.digest(eruptUser.getPassword());
                }
                String calcPwd = MD5Utils.digest(digestPwd +
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + account);
                if (pwd.equalsIgnoreCase(calcPwd)) {
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
        sessionService.put(SessionKey.USER_TOKEN + loginModel.getToken(), loginModel.getEruptUser().getId().toString(), eruptAuthConfig.getExpireTimeByLogin());
    }

    public Long getCurrentUid() {
        String token = request.getHeader(LoginInterceptor.ERUPT_HEADER_TOKEN);
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(LoginInterceptor.URL_ERUPT_PARAM_TOKEN);
        }
        return Long.valueOf(sessionService.get(SessionKey.USER_TOKEN + token).toString());
    }

    public EruptUser getCurrentEruptUser() {
        entityManager.clear();
        return entityManager.find(EruptUser.class, getCurrentUid());
    }

    public boolean verifyToken(String token) {
        if (null == sessionService.get(SessionKey.USER_TOKEN + token)) {
            return false;
        } else {
            return true;
        }
    }

    private static VL[] VLS;

    static {
        try {
            VLS = EruptMenu.class.getDeclaredField("path")
                    .getAnnotation(EruptField.class).edit().inputType().prefix();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public boolean verifyMenuAuth(String token, String name) {
        List<EruptMenu> menus = sessionService.get(SessionKey.MENU_LIST + token, new TypeToken<List<EruptMenu>>() {
        }.getType());
        for (EruptMenu menu : menus) {
            if (StringUtils.isNotBlank(menu.getPath()) && menu.getPath().toLowerCase().contains(name.toLowerCase())) {
                String path = menu.getPath();
                for (VL vl : VLS) {
                    if (vl.value().length() > 2 && path.contains(vl.value())) {
                        path = menu.getPath().replace(vl.value(), "");
                        break;
                    }
                }
                if (path.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean verifyEruptMenuAuth(String token, String authStr, EruptModel eruptModel) {
        //校验authStr与请求头erupt信息是否匹配，来验证其合法性
        if (!authStr.equalsIgnoreCase(eruptModel.getEruptName())) {
            return false;
        }
        //检验注解
        if (!eruptModel.getErupt().loginUse()) {
            return true;
        }
        //校验菜单权限
        {
            return verifyMenuAuth(token, eruptModel.getEruptName());
        }
    }

}
