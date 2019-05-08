package xyz.erupt.core.exception;

/**
 * @Description:
 * @author: liyuepeng
 * @time:2018/12/15 23:18
 */
public class EruptRuntimeException extends RuntimeException {


    public EruptRuntimeException(String message) {
        //TODO 需要记录IP或者其他操作
        super(message);
    }
}
