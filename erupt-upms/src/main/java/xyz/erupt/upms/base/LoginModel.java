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

    private boolean pass; //校验是否通过

    private boolean resetPwd = false; //是否需要重置密码

    private boolean useVerifyCode = false; //是否需要验证码

    private String reason; //未校验通过原因

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
