package xyz.erupt.core.exception;

/**
 * @author YuePeng
 * date 2020-09-30
 */
public class EruptWebApiRuntimeException extends RuntimeException {

    public EruptWebApiRuntimeException(String message) {
        super(message);
    }

    public EruptWebApiRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
