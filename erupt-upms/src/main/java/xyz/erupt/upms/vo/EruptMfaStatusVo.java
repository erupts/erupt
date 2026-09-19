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

    //whether the feature is switched on for this deployment
    private boolean enable;

    //whether the current user has an authenticator bound
    private boolean bound;

    //how many single use recovery codes are still unspent
    private int recoveryCodeCount;

}
