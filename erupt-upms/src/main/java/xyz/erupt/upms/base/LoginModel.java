package xyz.erupt.upms.base;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptUser;

import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2018-12-14.
 */
@Getter
@Setter
public class LoginModel {

    private transient EruptUser eruptUser;

    private boolean useVerifyCode = false; //是否需要验证码

    private boolean pass; //校验是否通过

    private String reason;  //未校验通过原因

    private String token;

    //--------------------------

    private LocalDateTime expire;

    private String userName;

    private String indexMenu;

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

    public void setEruptUser(EruptUser eruptUser) {
        this.eruptUser = eruptUser;
        EruptMenu indexMenu = eruptUser.getEruptMenu();
        this.setUserName(eruptUser.getName());
        if (null != indexMenu) {
            this.setIndexMenu(indexMenu.getType() + "||" + indexMenu.getValue());
        }
    }
}
