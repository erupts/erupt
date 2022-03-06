package xyz.erupt.core.exception;

import lombok.Getter;

import java.util.Map;

/**
 * @author YuePeng
 * date 2020-09-30
 */
@Getter
public class EruptWebApiRuntimeException extends RuntimeException {

    private boolean printStackTrace;

    private Map<String, Object> errorMap;

    public EruptWebApiRuntimeException(String message) {
        this(message, true);
    }

    public EruptWebApiRuntimeException(String message, boolean printStackTrace) {
        super(message);
        this.printStackTrace = printStackTrace;
    }

    public EruptWebApiRuntimeException(Map<String, Object> errorMap, boolean printStackTrace) {
        this.printStackTrace = printStackTrace;
        this.errorMap = errorMap;
    }

    public EruptWebApiRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
