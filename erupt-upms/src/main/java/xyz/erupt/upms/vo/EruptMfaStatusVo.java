package xyz.erupt.upms.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2026-09-18
 */
@Getter
@Setter
public class EruptMfaStatusVo {

    /** Whether the feature is switched on for this deployment */
    private boolean enable;

    /** Whether the current user has an authenticator bound */
    private boolean bound;

    /** How many single use recovery codes are still unspent */
    private int recoveryCodeCount;

}
