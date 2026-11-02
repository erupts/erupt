package xyz.erupt.upms.base;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.upms.model.EruptUser;

import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2018-12-14.
 */
@Getter
@Setter
public class LoginModel {

    /** Whether the credentials were accepted and a session was created */
    private boolean pass;

    /** Whether the user must change the password before going on */
    private boolean resetPwd = false;

    /** Whether the next attempt must carry a captcha */
    private boolean useVerifyCode = false;

    /** Password accepted, a one-time code is still owed: continue with login-mfa */
    private boolean mfaRequired = false;

    /** Short lived handle for the second step, never a session token */
    private String mfaTicket;

    /** Why the login was refused */
    private String reason;

    /** Session token, sent back in the token header */
    private String token;

    /** Who the session belongs to; a delegated login is the client's only way to know */
    private String account;

    /** When the token expires */
    private LocalDateTime expire;

    private transient EruptUser eruptUser;

    public LoginModel(boolean pass, EruptUser eruptUser) {
        this.pass = pass;
        this.setEruptUser(eruptUser);
    }

    public LoginModel(boolean pass, String reason) {
        this.pass = pass;
        this.reason = reason;
    }

    public LoginModel(boolean pass, String reason, boolean useVerifyCode) {
        this.pass = pass;
        this.reason = reason;
        this.useVerifyCode = useVerifyCode;
    }

    public LoginModel() {
    }

}
