package xyz.erupt.upms.spi;

import jakarta.servlet.http.HttpServletRequest;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.upms.base.LoginModel;
import xyz.erupt.upms.model.EruptUser;

/**
 * Extension point consulted at the security-sensitive moments of an account's life: before a
 * login attempt is honoured, before a session token is minted, and whenever a new password is
 * set. Unlike {@link xyz.erupt.upms.fun.LoginProxy}, which is singular and chosen by the
 * application through {@code @EruptLogin}, every Spring bean implementing this interface is
 * asked in order (Spring {@code @Order}), and the first non-null refusal wins: the login is
 * turned away or the password change is rejected.
 *
 * <p>A policy refusal is not a credential failure: it neither counts toward the login lock nor
 * triggers the captcha. Modules such as erupt-iam use this interface to layer password policies,
 * login risk control, session limits and mandatory MFA on top of the built-in checks without
 * touching the login flow itself.</p>
 *
 * @author YuePeng
 * date 2026/11/12
 */
public interface LoginPolicy {

    @Comment("Whether a login may be attempted at all from the requesting context. Return a translated refusal message, or null to let the attempt proceed. Runs before the account is looked up, after the built-in login lock")
    default String beforeLogin(String account, HttpServletRequest request) {
        return null;
    }

    @Comment("Last gate before a session token is minted. May mutate the login model, e.g. flag resetPwd for an expired password, or return a refusal message; null lets the token be issued. Covers every login path: password, delegated SSO and the MFA ticket exchange")
    default String beforeIssueToken(EruptUser eruptUser, LoginModel loginModel, HttpServletRequest request) {
        return null;
    }

    @Comment("Whether a newly chosen password is acceptable for this account. Return a translated violation message, or null to accept")
    default String validateNewPassword(EruptUser eruptUser, String rawPassword) {
        return null;
    }

    @Comment("Called after a password change was applied, inside the change's transaction. supersededPassword is the stored hash of the outgoing password — plain text for an account stored without encryption — and supersededSalt its salt; both were captured before the entity was rewritten. newPassword is the raw replacement")
    default void afterPasswordChange(EruptUser eruptUser, String supersededPassword, String supersededSalt, String newPassword) {
    }

}
