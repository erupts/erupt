package xyz.erupt.upms.service;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.spi.UpmsSettingsOverride;

import java.util.Objects;
import java.util.function.Function;

/**
 * The login and session settings in effect: the first {@link UpmsSettingsOverride} with an answer,
 * else the application configuration. Read these here rather than from {@link EruptUpmsProp} /
 * {@link EruptAppProp} directly, or a runtime override is silently ignored.
 *
 * @author YuePeng
 * date 2026/11/20
 */
@Service
public class EruptUpmsSettings {

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Resource
    private EruptAppProp eruptAppProp;

    @Resource
    private ObjectProvider<UpmsSettingsOverride> overrides;

    private <T> T resolve(Function<UpmsSettingsOverride, T> answer, T configured) {
        return overrides.orderedStream().map(answer).filter(Objects::nonNull).findFirst().orElse(configured);
    }

    public int sessionMinutes() {
        return resolve(UpmsSettingsOverride::sessionMinutes, eruptUpmsProp.getExpireTimeByLogin());
    }

    public boolean loginLockEnable() {
        return resolve(UpmsSettingsOverride::loginLockEnable, eruptUpmsProp.getLoginLock().isEnable());
    }

    public int loginLockMaxFailures() {
        return resolve(UpmsSettingsOverride::loginLockMaxFailures, eruptUpmsProp.getLoginLock().getMaxFailures());
    }

    public int loginLockMinutes() {
        return resolve(UpmsSettingsOverride::loginLockMinutes, eruptUpmsProp.getLoginLock().getLockMinutes());
    }

    public int verifyCodeCount() {
        return resolve(UpmsSettingsOverride::verifyCodeCount, eruptAppProp.getVerifyCodeCount());
    }

    public boolean resetPwdPrompt() {
        return resolve(UpmsSettingsOverride::resetPwdPrompt, Boolean.TRUE.equals(eruptAppProp.getResetPwdPrompt()));
    }

    public boolean mfaEnable() {
        return resolve(UpmsSettingsOverride::mfaEnable, eruptAppProp.getMfa().isEnable());
    }

}
