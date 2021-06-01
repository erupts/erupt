package xyz.erupt.upms.service;

import com.google.gson.Gson;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import xyz.erupt.core.config.EruptAppProp;
import xyz.erupt.core.config.EruptProp;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.config.EruptUpmsConfig;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.fun.EruptLogin;
import xyz.erupt.upms.fun.LoginProxy;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.log.EruptLoginLog;
import xyz.erupt.upms.util.IpUtil;
import xyz.erupt.upms.util.MD5Utils;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author YuePeng
 * date 2018-12-13.
 */
@Service
public class EruptUserService {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private EruptSessionService sessionService;

    @Resource
    private HttpServletRequest request;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptAppProp eruptAppProp;

    @Resource
    private EruptProp eruptProp;

    @Resource
    private EruptUpmsConfig eruptUpmsConfig;

    @Resource
    private EruptMenuService eruptMenuService;

    private final Gson gson = GsonFactory.getGson();

    public static final String LOGIN_ERROR_HINT = "账号或密码错误";

    public void cacheUserInfo(EruptUser eruptUser, String token) {
        List<EruptMenu> eruptMenus = eruptMenuService.getUserAllMenu(eruptUser);
        Map<String, Object> valueMap = new HashMap<>();
        Map<String, Object> codeMap = new HashMap<>();
        for (EruptMenu menu : eruptMenus) {
            codeMap.put(menu.getCode(), menu);
            if (null != menu.getValue()) {
                valueMap.put(menu.getValue(), menu);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (EruptRole role : eruptUser.getRoles()) {
            sb.append(role.getPowerOff()).append("|");
        }
        sessionService.putMap(SessionKey.MENU_VALUE_MAP + token, valueMap, eruptUpmsConfig.getExpireTimeByLogin());
        sessionService.putMap(SessionKey.MENU_CODE_MAP + token, codeMap, eruptUpmsConfig.getExpireTimeByLogin());
        sessionService.put(SessionKey.MENU_VIEW + token, gson.toJson(eruptMenuService.geneMenuListVo(eruptMenus)), eruptUpmsConfig.getExpireTimeByLogin());
        sessionService.put(SessionKey.ROLE_POWER + token, sb.toString(), eruptUpmsConfig.getExpireTimeByLogin());
    }

    public static LoginProxy findEruptLogin() {
        EruptLogin eruptLogin = EruptApplication.getPrimarySource().getAnnotation(EruptLogin.class);
        if (null != eruptLogin) {
            return EruptSpringUtil.getBean(eruptLogin.value());
        }
        return null;
    }

    private boolean loginErrorCountPlus(String ip) {
        Object loginError = sessionService.get(SessionKey.LOGIN_ERROR + ip);
        int loginErrorCount = 0;
        if (null != loginError) {
            loginErrorCount = Integer.parseInt(loginError.toString());
        }
        sessionService.put(SessionKey.LOGIN_ERROR + ip, ++loginErrorCount + "", eruptUpmsConfig.getExpireTimeByLogin());
        return loginErrorCount >= eruptAppProp.getVerifyCodeCount();
    }

    public LoginModel login(String account, String pwd) {
        String requestIp = IpUtil.getIpAddr(request);
        EruptUser eruptUser = this.findEruptUserByAccount(account);
        if (null != eruptUser) {
            if (!eruptUser.getStatus()) {
                return new LoginModel(false, "账号已锁定!");
            }
            if (StringUtils.isNotBlank(eruptUser.getWhiteIp())) {
                if (Arrays.stream(eruptUser.getWhiteIp().split("\n")).noneMatch(ip -> ip.equals(requestIp))) {
                    return new LoginModel(false, "当前 ip 无权访问");
                }
            }
            if (checkPwd(eruptUser, pwd)) {
                sessionService.put(SessionKey.LOGIN_ERROR + requestIp, "0", eruptUpmsConfig.getExpireTimeByLogin());
                return new LoginModel(true, eruptUser);
            } else {
                return new LoginModel(false, LOGIN_ERROR_HINT, loginErrorCountPlus(requestIp));
            }
        } else {
            return new LoginModel(false, LOGIN_ERROR_HINT, loginErrorCountPlus(requestIp));
        }
    }

    //校验密码
    public boolean checkPwd(EruptUser eruptUser, String pwd) {
        if (eruptAppProp.getPwdTransferEncrypt()) {
            String digestPwd;
            if (eruptUser.getIsMd5()) {
                digestPwd = eruptUser.getPassword();
            } else {
                digestPwd = MD5Utils.digest(eruptUser.getPassword());
            }
            String calcPwd = MD5Utils.digest(digestPwd + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + eruptUser.getAccount());
            return pwd.equalsIgnoreCase(calcPwd);
        } else {
            if (eruptUser.getIsMd5()) {
                pwd = MD5Utils.digest(pwd);
            }
            return pwd.equals(eruptUser.getPassword());
        }
    }

    public LocalDateTime getExpireTime() {
        if (eruptProp.isRedisSession()) {
            return LocalDateTime.now().plusMinutes(eruptUpmsConfig.getExpireTimeByLogin());
        } else {
            return LocalDateTime.now().plusSeconds(request.getSession().getMaxInactiveInterval());
        }
    }

    public boolean checkVerifyCode(String verifyCode) {
        String requestIp = IpUtil.getIpAddr(request);
        Object loginError = sessionService.get(SessionKey.LOGIN_ERROR + requestIp);
        long loginErrorCount = 0;
        if (null != loginError) {
            loginErrorCount = Long.parseLong(loginError.toString());
        }
        if (loginErrorCount >= eruptAppProp.getVerifyCodeCount()) {
            if (StringUtils.isBlank(verifyCode)) {
                return false;
            }
            Object vc = sessionService.get(SessionKey.VERIFY_CODE + requestIp);
            sessionService.remove(SessionKey.VERIFY_CODE + requestIp);
            return vc != null && vc.toString().equalsIgnoreCase(verifyCode);
        }
        return true;
    }

    @Transactional
    public void saveLoginLog(EruptUser user, String token) {
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        EruptLoginLog loginLog = new EruptLoginLog();
        loginLog.setToken(token);
        loginLog.setEruptUser(user);
        loginLog.setLoginTime(new Date());
        loginLog.setIp(IpUtil.getIpAddr(request));
        loginLog.setSystemName(userAgent.getOperatingSystem().getName());
        loginLog.setRegion(IpUtil.getCityInfo(loginLog.getIp()));
        loginLog.setBrowser(userAgent.getBrowser().getName() + " " + (userAgent.getBrowserVersion() == null ? "" : userAgent.getBrowserVersion().getMajorVersion()));
        loginLog.setDeviceType(userAgent.getOperatingSystem().getDeviceType().getName());
        entityManager.persist(loginLog);
    }

    @Transactional
    public EruptApiModel changePwd(String account, String pwd, String newPwd, String newPwd2) {
        if (!newPwd.equals(newPwd2)) {
            return EruptApiModel.errorNoInterceptApi("修改失败，新密码与确认密码不匹配");
        }
        EruptUser eruptUser = findEruptUserByAccount(account);
        LoginProxy loginProxy = EruptUserService.findEruptLogin();
        if (null != loginProxy) {
            loginProxy.beforeChangePwd(eruptUser, newPwd);
        }
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

    private EruptUser findEruptUserByAccount(String account) {
        return eruptDao.queryEntity(EruptUser.class, "account = :account", new HashMap<String, Object>(1) {{
            this.put("account", account);
        }});
    }

    //当前用户菜单中，通过编码获取菜单
    public EruptMenu getEruptMenuByCode(String menuValue) {
        return sessionService.getMapValue(SessionKey.MENU_CODE_MAP + getCurrentToken(), menuValue, EruptMenu.class);
    }

    //当前用户菜单中，通过菜单类型值获取菜单
    public EruptMenu getEruptMenuByValue(String menuValue) {
        return sessionService.getMapValue(SessionKey.MENU_VALUE_MAP + getCurrentToken(), menuValue, EruptMenu.class);
    }

    //获取当前请求token
    public String getCurrentToken() {
        String token = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN);
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_TOKEN);
        }
        return token;
    }

    //获取当前菜单对象
    public EruptMenu getCurrentEruptMenu() {
        String erupt = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_KEY);
        if (StringUtils.isBlank(erupt)) {
            erupt = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_KEY);
        }
        Assert.notNull(erupt, "request header 'erupt' not found ");
        return sessionService.getMapValue(SessionKey.MENU_VALUE_MAP + getCurrentToken(), erupt, EruptMenu.class);
    }

    //获取当前用户ID
    public Long getCurrentUid() {
        Object uid = sessionService.get(SessionKey.USER_TOKEN + getCurrentToken());
        return null == uid ? null : Long.valueOf(uid.toString());
    }

    //获取当前登录用户对象
    public EruptUser getCurrentEruptUser() {
        entityManager.clear();
        Long uid = this.getCurrentUid();
        return null == uid ? null : entityManager.find(EruptUser.class, uid);
    }

}
