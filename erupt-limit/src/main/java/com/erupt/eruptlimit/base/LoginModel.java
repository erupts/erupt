package com.erupt.eruptlimit.base;

import com.erupt.eruptlimit.model.EruptUser;
import lombok.Getter;

/**
 * Created by liyuepeng on 2018-12-14.
 */
@Getter
public class LoginModel {
    private boolean pass;

    private EruptUser eruptUser;

    private String reason;

    private boolean useVerifyCode = false;

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
