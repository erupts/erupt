package xyz.erupt.upms.base;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2026-06-08.
 */
@Getter
@Setter
public class LoginBody {

    private String account;

    private String pwd;

    // Verification code
    private String verifyCode;

    // Verification code identifier
    private String verifyCodeMark;

}
