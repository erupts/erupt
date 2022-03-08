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

    //异常信息扩展
    private Map<String, Object> extraMap;

    public EruptWebApiRuntimeException(String message) {
        this(message, true);
    }

    public EruptWebApiRuntimeException(String message, boolean printStackTrace) {
        super(message);
        this.printStackTrace = printStackTrace;
    }

    public EruptWebApiRuntimeException(Map<String, Object> extraMap, boolean printStackTrace) {
        this.printStackTrace = printStackTrace;
        this.extraMap = extraMap;
    }

    public EruptWebApiRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
