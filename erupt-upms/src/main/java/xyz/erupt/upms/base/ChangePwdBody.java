package xyz.erupt.upms.base;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2026-06-08.
 */
@Getter
@Setter
public class ChangePwdBody {

    /** Current password, encoded like the login password */
    private String pwd;

    /** New password, encoded like the login password */
    private String newPwd;

    /** New password repeated */
    private String newPwd2;

}
