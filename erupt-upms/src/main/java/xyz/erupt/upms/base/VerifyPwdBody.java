package xyz.erupt.upms.base;

import lombok.Getter;
import lombok.Setter;

/**
 * Password re-check for an already signed-in user, e.g. unlocking the screen.
 */
@Getter
@Setter
public class VerifyPwdBody {

    /** Password, encoded like the login password */
    private String pwd;

}
