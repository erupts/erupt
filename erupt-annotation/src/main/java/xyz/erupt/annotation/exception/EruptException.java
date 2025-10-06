package xyz.erupt.annotation.exception;

/**
 * @author YuePeng
 * date 2025/10/6 22:39
 */
public class EruptException extends RuntimeException {

    public EruptException(String message) {
        super(message);
    }

    public EruptException(String message, Throwable cause) {
        super(message, cause);
    }
}
