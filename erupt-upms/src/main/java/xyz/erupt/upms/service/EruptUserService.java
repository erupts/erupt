package xyz.erupt.upms.service;

import com.google.gson.Gson;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.service.EruptFileService;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.EncryptUtil;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.base.ProfileBody;
import xyz.erupt.upms.constant.EncryptType;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.fun.EruptLogin;
import xyz.erupt.upms.fun.LoginProxy;
import xyz.erupt.upms.helper.UpmsSecurityHelper;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.log.EruptLoginLog;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.util.IpUtil;
import xyz.erupt.upms.util.IpWhiteListMatcher;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private EruptFileService eruptFileService;

    @Resource
    private EruptAppProp eruptAppProp;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptContextService eruptContextService;

    @Resource
    private EruptTokenService eruptTokenService;

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
        EruptUpmsProp.LoginLock lock = eruptUpmsProp.getLoginLock();
        if (lock.isEnable() && loginErrorCount >= lock.getMaxFailures()) {
            // the counter is dropped with the lock so the next window starts clean when it lifts
            sessionService.put(SessionKey.LOGIN_LOCK + account + ":" + ip, "1", lock.getLockMinutes(), TimeUnit.MINUTES);
            sessionService.remove(key);
            log.warn("login locked for {} minutes after {} failures: {} @ {}", lock.getLockMinutes(), loginErrorCount, account, ip);
        }
        return loginErrorCount >= eruptAppProp.getVerifyCodeCount();
    }

    // Whether this account is still serving a lock from earlier failures made from the requesting IP
    public boolean isLoginLocked(String account) {
        return eruptUpmsProp.getLoginLock().isEnable()
                && sessionService.exist(SessionKey.LOGIN_LOCK + account + ":" + IpUtil.getIpAddr(request));
    }

    public LoginModel lockedLoginModel() {
        String reason = I18nTranslate.$translate("upms.login_locked")
                .replace("{0}", String.valueOf(eruptUpmsProp.getLoginLock().getLockMinutes()));
        return new LoginModel(false, reason, true);
    }

    /**
     * Record one failed login submission for the account from the requesting IP and build the
     * refusal. A wrong captcha counts the same as a wrong password: the lock limits how often a
     * pair may fail at the login endpoint at all, otherwise the captcha step would be a place to
     * retry forever. The failure that tips the counter over already answers with the lock.
     */
    public LoginModel loginFailure(String account, String reasonKey) {
        boolean useVerifyCode = this.loginErrorCountPlus(account, IpUtil.getIpAddr(request));
        if (this.isLoginLocked(account)) return this.lockedLoginModel();
        return new LoginModel(false, I18nTranslate.$translate(reasonKey), useVerifyCode);
    }

    public LoginModel login(String account, String pwd) {
        // Checked before the password so a locked pair cannot keep probing, right or wrong
        if (this.isLoginLocked(account)) return this.lockedLoginModel();
        EruptUser eruptUser = this.findEruptUserByAccount(account);
        if (null != eruptUser) {
            String reason = this.checkAccountUsable(eruptUser);
            if (null != reason) return new LoginModel(false, reason);
            if (this.checkPwd(eruptUser, pwd)) {
                sessionService.remove(SessionKey.LOGIN_ERROR + account + ":" + IpUtil.getIpAddr(request));
                return new LoginModel(true, eruptUser);
            }
        }
        return this.loginFailure(account, "upms.account_pwd_error");
    }

    /**
     * Whether this account may hold a session at all, whatever it used to prove itself.
     * Returns what stands in the way, or null when nothing does. Every login path owes the
     * account these three checks, so a delegated sign-on does not become a way around them.
     */
    public String checkAccountUsable(EruptUser eruptUser) {
        if (!eruptUser.getStatus()) return "Account has been locked.!";
        if (null != eruptUser.getExpireDate() && eruptUser.getExpireDate().getTime() < System.currentTimeMillis()) {
            return String.format("The account has become invalid at %s.", DateUtil.getSimpleFormatDate(eruptUser.getExpireDate()));
        }
        if (!IpWhiteListMatcher.isAllowed(IpUtil.getIpAddr(request), eruptUser.getWhiteIp())) {
            return "Your IP address does not have the authority to access.";
        }
        return null;
    }

    /**
     * Mint the session token and record the login. Called once every factor has passed,
     * by whichever flow got the user this far. Transactional because the login log is
     * written from here, and a self call would never reach the proxy that opens one.
     */
    @Transactional
    public void completeLogin(LoginModel loginModel, LoginProxy loginProxy) {
        EruptUser eruptUser = loginModel.getEruptUser();
        loginModel.setToken(Erupts.generateCode(22)); // 22 alphanumerics ~ 131 bits, meets the 128-bit session id guideline
        loginModel.setExpire(LocalDateTime.now().plusMinutes(eruptUpmsProp.getExpireTimeByLogin()));
        loginModel.setResetPwd(null == eruptUser.getResetPwdTime());
        loginModel.setAccount(eruptUser.getAccount());
        if (null != loginProxy) loginProxy.loginSuccess(eruptUser, loginModel.getToken());
        eruptTokenService.loginToken(eruptUser, loginModel.getToken());
        this.saveLoginLog(eruptUser, loginModel.getToken()); //Record login log
    }

    public boolean checkPwd(EruptUser eruptUser, String inputPwd) {
        return checkPwd(eruptUser.getPassword(), eruptUser.getEncrypt(), eruptUser.getSalt(), eruptUser.getEncryptType(), inputPwd);
    }

    public boolean checkPwd(String storedPassword, Boolean encrypt, String salt, String encryptType, String inputPwd) {
        return UpmsSecurityHelper.checkPwd(storedPassword, encrypt, salt, encryptType, inputPwd);
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

    /**
     * Confirm the current user's password. Without a LoginProxy this is the sign-in check itself,
     * lock and failure counter included; with one, the proxy decides exactly as it does at sign-in.
     */
    public R<Void> verifyPwd(String pwd) {
        String account = this.getCurrentAccount();
        LoginProxy loginProxy = findEruptLogin();
        if (null == loginProxy) {
            LoginModel loginModel = this.login(account, pwd);
            return loginModel.isPass() ? R.ok() : this.silentError(loginModel.getReason());
        }
        try {
            if (null == loginProxy.login(account, pwd)) return this.silentError(I18nTranslate.$translate("upms.account_pwd_error"));
            return R.ok();
        } catch (Exception e) {
            return this.silentError(e.getMessage());
        }
    }

    // The caller renders the reason itself, so the client must not also toast it
    private R<Void> silentError(String message) {
        R<Void> r = R.error(message);
        r.setPromptWay(R.PromptWay.NONE);
        return r;
    }

    @Transactional
    public R<Void> changePwd(String account, String pwd, String newPwd, String newPwd2) {
        if (!newPwd.equals(newPwd2)) {
            return R.error(I18nTranslate.$translate("upms.change_pwd_inconsistent"));
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
                return R.error(I18nTranslate.$translate("upms.change_pwd_same_as_old"));
            }
            if (eruptUser.getEncrypt()) {
                String salt = EncryptUtil.generateSalt();
                eruptUser.setSalt(salt);
                eruptUser.setEncryptType(EncryptType.SHA512);
                eruptUser.setPassword(EncryptUtil.digestSHA512Salt(newPwd, salt));
            } else {
                eruptUser.setSalt(null);
                eruptUser.setEncryptType(null);
                eruptUser.setPassword(newPwd);
            }

            eruptUser.setResetPwdTime(new Date());
            eruptDao.getEntityManager().merge(eruptUser);
            // A new password ends every other session: whoever held the old one is out
            eruptTokenService.logoutOtherTokens(account, eruptContextService.getCurrentToken());
            if (null != loginProxy) {
                loginProxy.afterChangePwd(eruptUser, pwd, newPwd);
            }
            return R.ok();
        } else {
            return R.error(I18nTranslate.$translate("upms.pwd_error"));
        }
    }

    // Extensions a self-service avatar may carry: raster formats a browser renders without a script surface
    private static final List<String> AVATAR_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");

    private static final int AVATAR_MAX_KB = 2048;

    public String uploadAvatar(MultipartFile file) {
        String filename = StringUtils.defaultString(file.getOriginalFilename());
        String extension = filename.substring(filename.lastIndexOf('.') + 1);
        if (!AVATAR_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.profile.avatar_format") + ": " + String.join(", ", AVATAR_EXTENSIONS));
        }
        if (file.getSize() / 1024 > AVATAR_MAX_KB) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.profile.avatar_size") + ": " + AVATAR_MAX_KB + "KB");
        }
        return eruptFileService.upload(file, "/avatar" + eruptFileService.createPath(file));
    }

    @Transactional
    public void updateProfile(ProfileBody profile) {
        EruptUser eruptUser = this.getCurrentEruptUser();
        String name = StringUtils.trimToNull(profile.getName());
        if (null == name) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.profile.name_required"));
        }
        // Only an uploaded path or an absolute web URL; anything else (javascript:, data:) is not an avatar
        String avatar = StringUtils.trimToNull(profile.getAvatar());
        if (null != avatar && !(avatar.startsWith("/") || avatar.startsWith("http://") || avatar.startsWith("https://"))) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.profile.avatar_invalid"));
        }
        Optional.ofNullable(findEruptLogin()).ifPresent(it -> it.beforeUpdateProfile(eruptUser, profile));
        eruptUser.setName(name);
        eruptUser.setAvatar(avatar);
        eruptDao.getEntityManager().merge(eruptUser);
        eruptTokenService.renameUser(eruptContextService.getCurrentToken(), name);
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
        return getEruptMenuValues(eruptContextService.getCurrentToken());
    }

    public List<String> getEruptMenuValues(String token) {
        return sessionService.getMapKeys(SessionKey.MENU_VALUE_MAP + token);
    }

    public Map<String, Boolean> getEruptMenuValuesMap() {
        return getEruptMenuValuesMap(eruptContextService.getCurrentToken());
    }

    public Map<String, Boolean> getEruptMenuValuesMap(String token) {
        return getEruptMenuValues(token).stream().collect(Collectors.toMap(it -> it, it -> true));
    }

    // Current user's id in the platform (EruptUser) id space. A tenant session has no such id:
    // its uid lives in the tenant user table and collides with platform ids, so every consumer
    // (HyperModel audit columns, per-user queries in plugins, login checks) must see null instead.
    public Long getCurrentUid() {
        MetaUserinfo metaUserinfo = getSimpleUserInfo();
        if (null == metaUserinfo || null != metaUserinfo.getTenantId()) return null;
        return metaUserinfo.getId();
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
        Long uid = this.getCurrentUid(); // null for tenant sessions, see getCurrentUid()
        return null == uid ? null : eruptDao.getEntityManager().find(EruptUser.class, uid);
    }

}