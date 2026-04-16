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

    private boolean pass; //Whether validation passed

    private boolean resetPwd = false; //Whether password reset is required

    private boolean useVerifyCode = false; //Whether a verification code is required

    private String reason; //Reason for validation failure

    private String token;

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
