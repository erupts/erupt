package xyz.erupt.core.exception;

/**
 * @Description:
 * @author: liyuepeng
 * @time:2018/12/15 23:18
 */
public class EruptNoLegalPowerException extends RuntimeException {

    public static final String NO_LEGAL_POWER = "权限不足，该操作将被记录";

    public EruptNoLegalPowerException() {
        //TODO 需要记录IP或者其他操作
        super(NO_LEGAL_POWER);
    }
}
