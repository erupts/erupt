package xyz.erupt.core.exception;

import lombok.extern.java.Log;

/**
 * @Description:
 * @author: liyuepeng
 * @time:2018/12/15 23:18
 */
@Log
public class EruptNoLegalPowerException extends RuntimeException {

    private static final String NO_LEGAL_POWER = "权限不足，该操作将被记录!";

    public EruptNoLegalPowerException() {
        //TODO 需要记录IP或者其他操作
        super(NO_LEGAL_POWER);
        log.severe(super.toString());
    }
}
