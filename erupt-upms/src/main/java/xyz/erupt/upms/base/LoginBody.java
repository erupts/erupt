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

    /** Login account */
    private String account;

    /** Password; base64-encoded three times when erupt-app.pwd-transfer-encrypt is on */
    private String pwd;

    /** Captcha text, required once a previous answer returned useVerifyCode */
    private String verifyCode;

    /** The mark the captcha image was requested with */
    private String verifyCodeMark;

}
