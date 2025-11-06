package xyz.erupt.core.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @author YuePeng
 * date 2018-12-15
 */
@Slf4j
public class EruptNoLegalPowerException extends RuntimeException {

    private static final String NO_LEGAL_POWER = "Insufficient permissions. This operation will be recorded !";

    public EruptNoLegalPowerException() {
        this(NO_LEGAL_POWER);
    }

    public EruptNoLegalPowerException(String message) {
        if (null == message) {
            throw new EruptWebApiRuntimeException(NO_LEGAL_POWER);
        } else {
            throw new EruptWebApiRuntimeException(message);
        }
    }
}
