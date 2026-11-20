package xyz.erupt.upms.spi;

/**
 * Runtime overrides for the login and session settings that otherwise come from the application
 * configuration ({@code erupt.upms.*}, {@code erupt-app.*}). Consulted as ordered Spring beans
 * through {@link xyz.erupt.upms.service.EruptUpmsSettings}: the first non-null answer wins, null
 * keeps the configured value, so an empty bean list means the configuration alone. erupt-iam
 * answers from its security policy, which makes these editable without a restart.
 *
 * @author YuePeng
 * date 2026/11/20
 */
public interface UpmsSettingsOverride {

    /** Session lifetime in minutes, renewed on activity ({@code erupt.upms.expire-time-by-login}) */
    default Integer sessionMinutes() {
        return null;
    }

    /** Whether repeated password failures lock the account + IP pair ({@code erupt.upms.login-lock.enable}) */
    default Boolean loginLockEnable() {
        return null;
    }

    /** Consecutive failures before the lock ({@code erupt.upms.login-lock.max-failures}) */
    default Integer loginLockMaxFailures() {
        return null;
    }

    /** How long the lock lasts ({@code erupt.upms.login-lock.lock-minutes}) */
    default Integer loginLockMinutes() {
        return null;
    }

    /** Failures before the captcha appears, 0 for always ({@code erupt-app.verify-code-count}) */
    default Integer verifyCodeCount() {
        return null;
    }

    /** Whether users still on their initial password are prompted to change it ({@code erupt-app.reset-pwd-prompt}) */
    default Boolean resetPwdPrompt() {
        return null;
    }

    /** Whether TOTP multi-factor authentication is offered ({@code erupt-app.mfa.enable}) */
    default Boolean mfaEnable() {
        return null;
    }

}
