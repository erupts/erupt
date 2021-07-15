package xyz.erupt.core.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @author YuePeng
 * date 2018-12-15
 */
@Slf4j
public class EruptNoLegalPowerException extends RuntimeException {

    private static final String NO_LEGAL_POWER = "权限不足，该操作将被记录!";

    public EruptNoLegalPowerException() {
        this(NO_LEGAL_POWER);
    }

    public EruptNoLegalPowerException(String message) {
        //TODO 记录IP或者其他操作
        if (null == message) {
            throw new EruptWebApiRuntimeException(NO_LEGAL_POWER);
        } else {
            throw new EruptWebApiRuntimeException(message);
        }
    }
}
