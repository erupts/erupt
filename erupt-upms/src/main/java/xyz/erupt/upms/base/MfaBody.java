package xyz.erupt.upms.base;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2026-09-18
 */
@Getter
@Setter
public class MfaBody {

    // Ticket issued by /login once the password matched, only used by the login flow
    private String mfaTicket;

    // One-time code from the authenticator, or a recovery code
    private String code;

    // Current password, required when removing a binding
    private String pwd;

}
