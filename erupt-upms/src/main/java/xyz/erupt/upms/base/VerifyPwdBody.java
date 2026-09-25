package xyz.erupt.upms.base;

import lombok.Getter;
import lombok.Setter;

/**
 * Password re-check for an already signed-in user, e.g. unlocking the screen.
 */
@Getter
@Setter
public class VerifyPwdBody {

    private String pwd;

}
