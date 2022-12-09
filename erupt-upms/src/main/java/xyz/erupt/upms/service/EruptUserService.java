package xyz.erupt.upms.service;

import com.google.gson.Gson;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.MD5Util;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.fun.EruptLogin;
import xyz.erupt.upms.fun.LoginProxy;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.log.EruptLoginLog;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.util.IpUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2018-12-13.
 */
@Service
public class EruptUserService {

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
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptContextService eruptContextService;

    @Resource
    private EruptMenuService eruptMenuService;

    private final Gson gson = GsonFactory.getGson();

    public static final String LOGIN_ERROR_HINT = "账号或密码错误";

    public void cacheUserInfo(EruptUser eruptUser, String token) {
        List<EruptMenu> eruptMenus = eruptMenuService.getUserAllMenu(eruptUser);
        Map<String, Object> valueMap = new HashMap<>();
        for (EruptMenu menu : eruptMenus) {
            if (null != menu.getValue()) {
                //根据URL规则?后的均是参数如：eruptTest?code=test，把参数 ?code=test 去除
                valueMap.put(menu.getValue().toLowerCase().split("\\?")[0], menu);
            }
        }
        sessionService.putMap(SessionKey.MENU_VALUE_MAP + token, valueMap, eruptUpmsProp.getExpireTimeByLogin());
        sessionService.put(SessionKey.MENU_VIEW + token, gson.toJson(eruptMenuService.geneMenuListVo(eruptMenus)), eruptUpmsProp.getExpireTimeByLogin());
        MetaUserinfo metaUserinfo = new MetaUserinfo();
        metaUserinfo.setId(eruptUser.getId());
        metaUserinfo.setSuperAdmin(eruptUser.getIsAdmin());
        metaUserinfo.setAccount(eruptUser.getAccount());
        metaUserinfo.setUsername(eruptUser.getName());
        metaUserinfo.setRoles(eruptUser.getRoles().stream().map(EruptRole::getCode).collect(Collectors.toList()));
        Optional.ofNullable(eruptUser.getEruptPost()).ifPresent(it -> metaUserinfo.setPost(it.getCode()));
        Optional.ofNullable(eruptUser.getEruptOrg()).ifPresent(it -> metaUserinfo.setOrg(it.getCode()));
        sessionService.put(SessionKey.USER_INFO + token, gson.toJson(metaUserinfo), eruptUpmsProp.getExpireTimeByLogin());
    }


    public static LoginProxy findEruptLogin() {
        if (null == EruptApplication.getPrimarySource()) {
            throw new RuntimeException("Not found '@EruptScan' Annotation");
        }
        EruptLogin eruptLogin = EruptApplication.getPrimarySource().getAnnotation(EruptLogin.class);
        if (null != eruptLogin) {
            return EruptSpringUtil.getBean(eruptLogin.value());
        }
        return null;
    }

    private boolean loginErrorCountPlus(String account, String ip) {
        String key = SessionKey.LOGIN_ERROR + account + ":" + ip;
        Object loginError = sessionService.get(key);
        int loginErrorCount = 0;
        if (null != loginError) {
            loginErrorCount = Integer.parseInt(loginError.toString());
        }
        sessionService.put(key, ++loginErrorCount + "", eruptUpmsProp.getExpireTimeByLogin());
        return loginErrorCount >= eruptAppProp.getVerifyCodeCount();
    }

    public LoginModel login(String account, String pwd) {
        String requestIp = IpUtil.getIpAddr(request);
        EruptUser eruptUser = this.findEruptUserByAccount(account);
        if (null != eruptUser) {
            if (!eruptUser.getStatus()) return new LoginModel(false, "账号已锁定!");
            if (null != eruptUser.getExpireDate()) {
                if (eruptUser.getExpireDate().getTime() < System.currentTimeMillis()) {
                    return new LoginModel(false, String.format("账号在 %s 失效", DateUtil.getSimpleFormatDate(eruptUser.getExpireDate())));
                }
            }
            if (StringUtils.isNotBlank(eruptUser.getWhiteIp())) {
                if (Arrays.stream(eruptUser.getWhiteIp().split("\n")).noneMatch(ip -> ip.equals(requestIp))) {
                    return new LoginModel(false, "当前 ip 无权访问");
                }
            }
            if (checkPwd(eruptUser, pwd)) {
                request.getSession().invalidate();
                sessionService.remove(SessionKey.LOGIN_ERROR + account + ":" + requestIp);
                return new LoginModel(true, eruptUser);
            } else {
                return new LoginModel(false, LOGIN_ERROR_HINT, loginErrorCountPlus(account, requestIp));
            }
        } else {
            return new LoginModel(false, LOGIN_ERROR_HINT, loginErrorCountPlus(account, requestIp));
        }
    }

    //校验密码
    public boolean checkPwd(EruptUser eruptUser, String pwd) {
        if (eruptAppProp.getPwdTransferEncrypt()) {
            String digestPwd = eruptUser.getIsMd5() ? eruptUser.getPassword() : MD5Util.digest(eruptUser.getPassword());
            String calcPwd = MD5Util.digest(digestPwd + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + eruptUser.getAccount());
            return pwd.equalsIgnoreCase(calcPwd);
        } else {
            if (eruptUser.getIsMd5()) pwd = MD5Util.digest(pwd);
            return pwd.equals(eruptUser.getPassword());
        }
    }

    public LocalDateTime getExpireTime() {
        if (eruptProp.isRedisSession()) {
            return LocalDateTime.now().plusMinutes(eruptUpmsProp.getExpireTimeByLogin());
        } else {
            return LocalDateTime.now().plusSeconds(request.getSession().getMaxInactiveInterval());
        }
    }

    public boolean checkVerifyCode(String account, String verifyCode, String verifyCodeMark) {
        String requestIp = IpUtil.getIpAddr(request);
        Object loginError = sessionService.get(SessionKey.LOGIN_ERROR + account + ":" + requestIp);
        long loginErrorCount = 0;
        if (null != loginError) loginErrorCount = Long.parseLong(loginError.toString());
        if (loginErrorCount >= eruptAppProp.getVerifyCodeCount()) {
            if (StringUtils.isBlank(verifyCode)) return false;
            String key = SessionKey.VERIFY_CODE + verifyCodeMark;
            Object vc = sessionService.get(key);
            sessionService.remove(key);
            return vc != null && vc.toString().equalsIgnoreCase(verifyCode);
        }
        return true;
    }

    @Transactional
    public void saveLoginLog(EruptUser user, String token) {
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        EruptLoginLog loginLog = new EruptLoginLog();
        loginLog.setToken(token);
        loginLog.setUserName(user.getName());
        loginLog.setLoginTime(new Date());
        loginLog.setIp(IpUtil.getIpAddr(request));
        loginLog.setSystemName(userAgent.getOperatingSystem().getName());
        loginLog.setRegion(IpUtil.getCityInfo(loginLog.getIp()));
        loginLog.setBrowser(userAgent.getBrowser().getName() + " " + (userAgent.getBrowserVersion() == null ? "" : userAgent.getBrowserVersion().getMajorVersion()));
        loginLog.setDeviceType(userAgent.getOperatingSystem().getDeviceType().getName());
        eruptDao.getEntityManager().persist(loginLog);
    }

    @Transactional
    public EruptApiModel changePwd(String account, String pwd, String newPwd, String newPwd2) {
        if (!newPwd.equals(newPwd2)) {
            return EruptApiModel.errorNoInterceptMessage("修改失败，新密码与确认密码不匹配");
        }
        EruptUser eruptUser = findEruptUserByAccount(account);
        LoginProxy loginProxy = EruptUserService.findEruptLogin();
        if (null != loginProxy) {
            loginProxy.beforeChangePwd(eruptUser, newPwd);
        }
        if (eruptUser.getIsMd5()) {
            pwd = MD5Util.digest(pwd);
            newPwd = MD5Util.digest(newPwd);
        }
        if (eruptUser.getPassword().equals(pwd)) {
            if (newPwd.equals(eruptUser.getPassword())) {
                return EruptApiModel.errorNoInterceptMessage("修改失败，新密码不能和原始密码一样");
            }
            eruptUser.setPassword(newPwd);
            eruptUser.setResetPwdTime(new Date());
            eruptDao.getEntityManager().merge(eruptUser);
            if (null != loginProxy) {
                loginProxy.afterChangePwd(eruptUser, pwd, newPwd);
            }
            return EruptApiModel.successApi();
        } else {
            return EruptApiModel.errorNoInterceptMessage("密码错误");
        }
    }

    private EruptUser findEruptUserByAccount(String account) {
        return eruptDao.queryEntity(EruptUser.class, "account = :account", new HashMap<String, Object>(1) {{
            this.put("account", account);
        }});
    }

    //获取登录用户名
    public String getCurrentAccount() {
        Object account = sessionService.get(SessionKey.TOKEN_OLINE + eruptContextService.getCurrentToken());
        return null == account ? null : account.toString();
    }

    //从当前用户菜单中，通过菜单类型值获取菜单
    public EruptMenu getEruptMenuByValue(String menuValue) {
        return sessionService.getMapValue(SessionKey.MENU_VALUE_MAP + eruptContextService.getCurrentToken(), menuValue.toLowerCase(), EruptMenu.class);
    }

    public List<String> getEruptMenuValues() {
        return sessionService.getMapKeys(SessionKey.MENU_VALUE_MAP + eruptContextService.getCurrentToken());
    }

    public Map<String, Boolean> getEruptMenuValuesMap() {
        return getEruptMenuValues().stream().collect(Collectors.toMap(it -> it, it -> true));
    }

    //获取当前用户ID
    public Long getCurrentUid() {
        MetaUserinfo metaUserinfo = getSimpleUserInfo();
        return null == metaUserinfo ? null : metaUserinfo.getId();
    }

    //获取当前登录用户基础信息（缓存中查找）
    public MetaUserinfo getSimpleUserInfo() {
        Object info = sessionService.get(SessionKey.USER_INFO + eruptContextService.getCurrentToken());
        return null == info ? null : gson.fromJson(info.toString(), MetaUserinfo.class);
    }

    public MetaUserinfo getSimpleUserInfoByToken(String token) {
        Object info = sessionService.get(SessionKey.USER_INFO + token);
        return null == info ? null : gson.fromJson(info.toString(), MetaUserinfo.class);
    }

    //获取当前登录用户对象(数据库中查找)
    public EruptUser getCurrentEruptUser() {
        Long uid = this.getCurrentUid();
        return null == uid ? null : eruptDao.getEntityManager().find(EruptUser.class, uid);
    }

}
