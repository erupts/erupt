package xyz.erupt.upms.service;

import com.google.gson.Gson;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.MD5Util;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.constant.EncryptType;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.fun.EruptLogin;
import xyz.erupt.upms.fun.LoginProxy;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.log.EruptLoginLog;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.util.IpUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2018-12-13.
 */
@Service
@Slf4j
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
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptContextService eruptContextService;

    private final Gson gson = GsonFactory.getGson();

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
        sessionService.put(key, ++loginErrorCount + "", eruptUpmsProp.getExpireTimeByLogin(), TimeUnit.MINUTES);
        return loginErrorCount >= eruptAppProp.getVerifyCodeCount();
    }

    public LoginModel login(String account, String pwd) {
        String requestIp = IpUtil.getIpAddr(request);
        EruptUser eruptUser = this.findEruptUserByAccount(account);
        if (null != eruptUser) {
            if (!eruptUser.getStatus()) return new LoginModel(false, "Account has been locked.!");
            if (null != eruptUser.getExpireDate()) {
                if (eruptUser.getExpireDate().getTime() < System.currentTimeMillis()) {
                    return new LoginModel(false, String.format("The account has become invalid at %s.", DateUtil.getSimpleFormatDate(eruptUser.getExpireDate())));
                }
            }
            if (StringUtils.isNotBlank(eruptUser.getWhiteIp())) {
                if (Arrays.stream(eruptUser.getWhiteIp().split("\n")).noneMatch(ip -> ip.equals(requestIp))) {
                    return new LoginModel(false, "Your IP address does not have the authority to access.");
                }
            }
            if (this.checkPwd(eruptUser, pwd)) {
                sessionService.remove(SessionKey.LOGIN_ERROR + account + ":" + requestIp);
                return new LoginModel(true, eruptUser);
            }
        }
        return new LoginModel(false, I18nTranslate.$translate("upms.account_pwd_error"), loginErrorCountPlus(account, requestIp));
    }

    public boolean checkPwd(EruptUser eruptUser, String inputPwd) {
        return checkPwd(eruptUser.getPassword(), eruptUser.getEncrypt(), eruptUser.getSalt(), eruptUser.getEncryptType(), inputPwd);
    }

    public boolean checkPwd(String storedPassword, Boolean encrypt, String salt, String encryptType, String inputPwd) {
        String checkPwd;
        if (EncryptType.SHA512.equalsIgnoreCase(encryptType)) {
            checkPwd = MD5Util.digestSHA512Salt(inputPwd, salt);
        } else {
            checkPwd = encrypt ? MD5Util.digest(inputPwd) : inputPwd;
        }
        return checkPwd.equals(storedPassword);
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
            return EruptApiModel.errorMessageApi(I18nTranslate.$translate("upms.change_pwd_inconsistent"));
        }
        EruptUser eruptUser = findEruptUserByAccount(account);
        LoginProxy loginProxy = EruptUserService.findEruptLogin();
        if (null != loginProxy) {
            loginProxy.beforeChangePwd(eruptUser, newPwd);
        }

        // Verify with the stored encryption type
        boolean isValid = checkPwd(eruptUser, pwd);

        if (isValid) {
            if (checkPwd(eruptUser, newPwd)) {
                return EruptApiModel.errorMessageApi(I18nTranslate.$translate("upms.change_pwd_same_as_old"));
            }
            if (eruptUser.getEncrypt()) {
                String salt = MD5Util.generateSalt();
                eruptUser.setSalt(salt);
                eruptUser.setEncryptType(EncryptType.SHA512);
                eruptUser.setPassword(MD5Util.digestSHA512Salt(newPwd, salt));
            } else {
                eruptUser.setSalt(null);
                eruptUser.setEncryptType(null);
                eruptUser.setPassword(newPwd);
            }

            eruptUser.setResetPwdTime(new Date());
            eruptDao.getEntityManager().merge(eruptUser);
            if (null != loginProxy) {
                loginProxy.afterChangePwd(eruptUser, pwd, newPwd);
            }
            return EruptApiModel.successApi();
        } else {
            return EruptApiModel.errorMessageApi(I18nTranslate.$translate("upms.pwd_error"));
        }
    }

    private EruptUser findEruptUserByAccount(String account) {
        return eruptDao.lambdaQuery(EruptUser.class).eq(EruptUser::getAccount, account).one();
    }

    //Get the logged-in account name
    public String getCurrentAccount() {
        Object account = sessionService.get(SessionKey.TOKEN_OLINE + eruptContextService.getCurrentToken());
        return null == account ? null : account.toString();
    }

    //Get a menu from the current user's menu list by menu type value
    public EruptMenu getEruptMenuByValue(String menuValue) {
        return getEruptMenuByValue(menuValue, eruptContextService.getCurrentToken());
    }

    public EruptMenu getEruptMenuByValue(String menuValue, String token) {
        return sessionService.getMapValue(SessionKey.MENU_VALUE_MAP + token, menuValue.toLowerCase(), EruptMenu.class);
    }

    public List<String> getEruptMenuValues() {
        return sessionService.getMapKeys(SessionKey.MENU_VALUE_MAP + eruptContextService.getCurrentToken());
    }

    public Map<String, Boolean> getEruptMenuValuesMap() {
        return getEruptMenuValues().stream().collect(Collectors.toMap(it -> it, it -> true));
    }

    //Get the current user ID
    public Long getCurrentUid() {
        MetaUserinfo metaUserinfo = getSimpleUserInfo();
        return null == metaUserinfo ? null : metaUserinfo.getId();
    }

    //Get basic info of the currently logged-in user (from cache)
    public MetaUserinfo getSimpleUserInfo() {
        Object info = sessionService.get(SessionKey.USER_INFO + eruptContextService.getCurrentToken());
        return null == info ? null : gson.fromJson(info.toString(), MetaUserinfo.class);
    }

    public MetaUserinfo getSimpleUserInfoByToken(String token) {
        Object info = sessionService.get(SessionKey.USER_INFO + token);
        return null == info ? null : gson.fromJson(info.toString(), MetaUserinfo.class);
    }

    //Get the current logged-in user object (from database)
    public EruptUser getCurrentEruptUser() {
        Long uid = this.getCurrentUid();
        return null == uid ? null : eruptDao.getEntityManager().find(EruptUser.class, uid);
    }

}