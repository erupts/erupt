package xyz.erupt.upms.base;

import lombok.Data;
import xyz.erupt.upms.model.EruptUser;

import java.time.LocalDateTime;

/**
 * @author liyuepeng
 * @date 2018-12-14.
 */
@Data
public class LoginModel {

    private transient EruptUser eruptUser;

    private boolean useVerifyCode = false;

    private LocalDateTime expire;

    private boolean pass;

    private String reason;

    private String token;

    private String userName;

    private String indexMenu;

    public LoginModel(boolean pass, EruptUser eruptUser) {
        this.pass = pass;
        this.eruptUser = eruptUser;
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
}
