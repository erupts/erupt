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

    private String pwd;

    private String newPwd;

    private String newPwd2;

}
